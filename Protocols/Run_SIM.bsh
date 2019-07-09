//--------------------------------------------------------------------- 
//Shepherd Laboratory LED-SIM control script
//Gabriel Martinez
//Nathaniel Britten
//Douglas Shepherd, PhD
//Department of Pharmacology
//University of Colorado Anschutz Medical Campus
//Contact info: douglas.shepherd@ucdenver.edu
//v0.3
//2019.04.09
//------------------------------------------------------------------------------
//!!!For MicroManager 2.0 GAMMA!!!
//------------------------------------------------------------------------------
//This script takes the settings from the MDA window and runs a SIM acquistion
//using the stack file containing DMD patterns.
//------------------------------------------------------------------------------

//Imports
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.data.Metadata;
import org.micromanager.data.Metadata.MetadataBuilder;
import org.micromanager.display.DisplayWindow;
import org.micromanager.MultiStagePosition;
import org.micromanager.SequenceSettings;
import mmcorej.TaggedImage;
import ij.io.Opener; 
import ij.ImagePlus;
import ij.ImageStack;

//Assign mmc to the Core
mmc = mm.getCore();

//Load image stack containing DMD patterns
Opener opener = new Opener();
ImagePlus imp = opener.openImage("C:\\Users\\shepherdlab\\Desktop\\SIM\\SIM-period_4.tif");
ImageStack SIMstack = imp.getImageStack();
numSIMimages = SIMstack.getSize();

//Pull acquisition settings from MDA window
acqSettings = mm.acquisitions().getAcquisitionSettings();
rootDir = acqSettings.root;
rootDirUnique = mm.data().getUniqueSaveDirectory(rootDir);

//Initialize Texas Instrument DMD using genericSLM device
//The DMD must be put into listening mode on the DisplayPort using the TI GUI before starting MM 2.0
DMD = mmc.getSLMDevice();

//Initialize XYZ stages
xyStage = mmc.getXYStageDevice();
zStage = mmc.getFocusDevice();

//Initialize autofocus manager
aFocus = mm.getAutofocusManager();

//Create multipagetiff datatstore. Need SSD drive to keep up with imaging.
Datastore store = mm.data().createMultipageTIFFDatastore(rootDirUnique,true,true);

//Create a display for the datastore
DisplayWindow display = mm.displays().createDisplay(store);
mm.displays().manage(store);

//Pull (XYZ) position list from MDA window
positionList = mm.positions().getPositionList();

//Throw error if multiple positions is not enabled
if (positionList == null) {
	mm.scripter().message("No Positionlist found");
	exit;
}

//Throw error if no positions have been defined
if (positionList.getNumberOfPositions() < 1) {
	mm.scripter().message("PositionList is empty");
	exit;
}

//Create Coordinates builder object
Coords.CoordsBuilder builder = mm.data().getCoordsBuilder();

//Execute multipoint SIM acquisition
for (int stagePos=0; stagePos<positionList.getNumberOfPositions(); ++stagePos) {
	//Get new XYZ position
	position=positionList.getPosition(stagePos);

	mm.scripter().message("Number of XY positions: "+positionList.getNumberOfPositions());
	mm.scripter().message("Current XY positions: "+stagePos);

	//Move to new XYZ position
	MultiStagePosition.goToPosition(position, mm.core());
	
	//Wait for XY stage. Our XY stage is much slower than Z stage, so waiting for it 
	//to finish the move is reasonable.
	mm.core().waitForDevice(xyStage);

	//DEBUG: Check to make sure z position is altered by autofocus
	//mm.scripter().message("Z position: "+mmc.getPosition(zStage));

	//Execute autofocus that is setup through plugin in MDA window
	// mm.getAutofocusManager().getAutofocusMethod().fullFocus();
		
	//Record current z position after autofocus
	currentzPos = mmc.getPosition(zStage);

	//DEBUG: Check to make sure z position is altered by autofocus
	//mm.scripter().message("Z position: "+currentzPos);

	//Tracker for which z slice the datastore should use
	int zTracker = 0;
	
	//Run z-stack
	for (z : acqSettings.slices) {

		//Move to relative Z position
		mmc.setPosition(zStage,currentzPos + z);
				
		//Tracker for which channel the datastore should use
		int channelTracker = 0;
		
		for  (channel : acqSettings.channels) {

			//Pull channel settings from acquistion settings
			channelColor = channel.color;
    		channelConfig = channel.config;
   		channelExposureMs = channel.exposure;
   		
			//Assign channel settings to the core
			mmc.setConfig("Channel",channelConfig);
			
			
			//Build SIM image for each channel
			for (int SIMpattern=1;SIMpattern<=numSIMimages;++SIMpattern) {
				
				//Load pattern onto DMD
				pixels = SIMstack.getPixels(SIMpattern);
				
				//Upload the image to the SLM.
				mmc.setSLMImage(DMD, pixels);
				
				//Activate the uploaded image on the SLM.
				mmc.displaySLMImage(DMD);

				//Pull current position
				xNow = position.getX();
				yNow = position.getY();

				//Grab image
				mmc.setCameraDevice("HamamatsuHam_DCAM2");
				mmc.setExposure(channelExposureMs);
				mmc.snapImage();
				TaggedImage tmp = mmc.getTaggedImage();
				Image image = mm.data().convertTaggedImage(tmp);
				
				//Create metadata object for image
				MetadataBuilder mdb = image.getMetadata().copy();
				md = mdb.positionName("Pos-"+stagePos).xPositionUm(xNow).yPositionUm(yNow).zPositionUm(z).
					exposureMs(channelExposureMs).build();
					
				//Build datastore coordinates for image
				//SIM patterns are currently stored in the time channel
   			builder.stagePosition(stagePos).z(zTracker).channel(channelTracker).time(SIMpattern-1);
   			Coords coords = builder.build();

   			//Create second image with metadata and datastore coordinates
				image_toDataStore = image.copyWith(coords, md);
				
				//Place images into dataStore
				// store.putImage(image1a);
				store.putImage(image_toDataStore);
			}
			//Increment the channel tracker
			++channelTracker;	
		}
		//Increment the z tracker
		++zTracker;	
	}
}

//Finish writing data to disk
store.freeze();

//Close dataStore object
store.close();	

