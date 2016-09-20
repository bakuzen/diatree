Author: Casey Kennington caseykennington[at]boisestate[dot].edu


**To run the diatree example:**

Make sure you have Java JDK1.7 or newer.

Then, you need gradle....

Ubuntu: 
sudo add-apt-repository ppa:cwchien/gradle
sudo apt-get update
sudo apt-get install gradle

MacOS: 
brew install gradle


Then, you need a Google ASR API Key. See https://www.chromium.org/developers/how-tos/api-keys


Then from within the diatree/Code/diatree directory, run:
gradle runMain -PapiKey=INSERT_YOUR_API_KEY_HERE


Checkpoint: gradle might take a while the first time it runs (it has to download dependencies), but you should see the diatree open in your default web browser and you should be able to use your default microphone (sorry, at the moment the example diatree only runs for German)


**To make your own diatree:**

Look at the example in domains/sigdial. 

The intent json file is required. It sets up the root diatree that is initially displayed. The intents.json file has a required "intents" root note name, as well as a required "root" child with the possible intents as children. Then note that each possible intent has its own name and the children correspond to the slots that could be filled. Each slot then has a corresponding .json file that contains the possible slot values as well as example training data. See domains/sigdial/json/food.json for an example. 

When your .json files are ready, you can run utils.InsertJsonData.java. Set your domain to the name of your new domain (which must be in the domains directory). Running this script will read your json files, insert the necessary information into a new sqlite database and run the training based on the training examples you provided. (At the moment, you need to provide at least a few training examples or it won't work.)

If you want to test your domain, you need to open the config/iu-config.xml file and change the inlu.domain property from "sigdial" to the name of your domain. Then you can run the gradle command above.


