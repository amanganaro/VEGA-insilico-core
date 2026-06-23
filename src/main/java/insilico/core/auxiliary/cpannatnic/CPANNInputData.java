
/**
 * Copyright (C) 2016 National Institute of Chemistry, Hajdrihova 19, 1001 Ljubljana, Slovenia. 
 * @author Viktor Drgan
 */

/*
*This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License along
*    with this program; if not, write to the Free Software Foundation, Inc.,
*    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package insilico.core.auxiliary.cpannatnic;//put different package name

import java.util.HashMap;

public class CPANNInputData implements Cloneable {
            private double[][] mydatatobetested;
            private double[][] mycpannweights;
            private int numberofobjects;
            private int numberofdescriptorsplustargets;
            private int numberoftargets;
            private int[] descriptorortarget;
            private int[] objectID;
            private int numberofneuronsinXdirection;
            private int numberofneuronsinYdirection;
            private double[][] targetpredictions;
            private double[] euclideandistance;
            private String[] objectisinapplicabilitydomain;
            private int[] neuronXposition;
            private int[] neuronYposition;
            private int[] isdescriptortargetselected;
            private String textdelimiterused;
            private int[] isdescriptorincludedintesting;
            private int[] objectsASCIIcode;
            private int[] objectdatasetnumber;        
            
            private double[] trainingparameters;     
            private boolean trainingparameterssaved;
            private int[] parametersfromopenedcpannweightsfile;
            private java.io.File mytemporarydirectory;
            
            private String datasetfilename;
            private java.awt.Color datasetcolor;
            private java.awt.Color[] datasetcolorforobjects;
           // private java.awt.Color classcolor;
            private HashMap classcolorsinhashmap;
            private java.awt.Color[] classcolorforobjects;
            
            private CPANNInputData mypreviousdata;
            private int datasetnumberofmypreviousdata;
            
            private double[] normalizationmean;
            private double[] normalizationstandarddeviation;
            
            private boolean thisisnormalizationset;//contain data to calculate normalization factors
            private boolean normalizationdatasaved;
            
            private String[] allVariableNames;//variable names obtained from input dataset
            private String[] allModelVariableNames;//variable name obtained from the model file (file with weights)
            
            private int inlinenumbertohigliht=-1;//should be programatically set greater than -1
            
            private java.awt.Point currentlyselectedneuron;
            
            public void saveCurrentlySelectedNeuronPosition(int xposition,int yposition){
                this.currentlyselectedneuron=new java.awt.Point(xposition,yposition);
            }
            public java.awt.Point getCurrentlySelectedNeuronPosition(){
                return this.currentlyselectedneuron;
            }
            
            public int getInlineNumberToHiglightWhenShowingSelectedNeuron(){
            return this.inlinenumbertohigliht;
              }
    
            public void setInlineNumberToHiglightWhenShowingSelectedNeuron(int inlinenumbertobehiglighted){
                this.inlinenumbertohigliht=inlinenumbertobehiglighted;
            }

            
             public void saveAllModelVariableNames(String[] varnamesfrommodelfile){
                 allModelVariableNames=varnamesfrommodelfile;
             }
             
             public String[] getAllModelVariableNames(){
                 return allModelVariableNames;
             }
            
            
            public void saveAllVariableNames(String[] tempvariablenames) {
                allVariableNames=tempvariablenames;
            }
            
            public String[] getAllVariableNames() {
                return allVariableNames;
            }          
            
            public void saveMyTemporaryPath(java.io.File tempmytemporarydirectory){
                mytemporarydirectory=tempmytemporarydirectory;
            }
            
            public java.io.File getMyTemporaryPath(){
                return mytemporarydirectory;
            }
            
            public void setNormalizationDataSaved(boolean arenormalizationdatasaved){
                normalizationdatasaved=arenormalizationdatasaved;
            }
            
            public boolean areNormalizationDataSaved(){
                return normalizationdatasaved;
            }
            
            public void setAsNormalizationSet(boolean isthisnormalizationset){
                thisisnormalizationset=isthisnormalizationset;
            }
            
            public boolean isThisNormalizationSet(){
                return thisisnormalizationset;
            }
                                        
            public void saveNormalizationMean(double[] calculatednormalizationmean){
                normalizationmean=calculatednormalizationmean;
            }
            
            public double[] getNormalizationMean(){
                return normalizationmean;
            }
                        
           public void saveNormalizationStandardDeviation(double[] calculatednormalizationstandarddeviation){
                normalizationstandarddeviation=calculatednormalizationstandarddeviation;
            }
            
            public double[] getNormalizationStandardDeviation(){
                return normalizationstandarddeviation;
            }
            
            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone(); //To change body of generated methods, choose Tools | Templates.
            }
            
            public void setClassColorsHashMap(HashMap tempclasscolorsinhashmap){
                classcolorsinhashmap=tempclasscolorsinhashmap;
            }
                        
            public HashMap getClassColorsInHashMap(){
                return classcolorsinhashmap;
            }
            
            public void setClassColorsForObjects(int hashkey,java.awt.Color tempselectedcolor){
                classcolorsinhashmap.put(hashkey, tempselectedcolor);              
            }
            
            public java.awt.Color getClassColorsForObjects(int hashkey){
                return (java.awt.Color)classcolorsinhashmap.get(hashkey);
            }
            
            public void setDatasetColorsForObjects(java.awt.Color[] tempcolors){
                datasetcolorforobjects=tempcolors;
            }
            
            public java.awt.Color[] getDatasetColorsForObjects(){
                return datasetcolorforobjects;
            }
            
            public void setDatasetColor(java.awt.Color tempcolor){
                datasetcolor=tempcolor;
            }
            
            public java.awt.Color getDatasetColor(){
                return datasetcolor;
            }
                        
            
            public void saveMyDatasetFilename(String tempdatasetfilename){
                datasetfilename=tempdatasetfilename;
            }
            
            public String getMyDatasetFilename(){
                return datasetfilename;
            }
            
            public void saveMyDatasetNumberOfMypreviousdata(int tempdatasetnumberid){
                datasetnumberofmypreviousdata=tempdatasetnumberid;
            }
            
            public int getMyDatasetNumberOfMypreviousdata(){
                return datasetnumberofmypreviousdata;
            }
            
            public void saveMypreviousdata(CPANNInputData tempmypreviousdata){
                mypreviousdata=tempmypreviousdata;
            }
            
            public CPANNInputData getMypreviousdata(){
                return mypreviousdata;
            }
            
            public void savecpannparametersinopenedfile(int[] params){
                parametersfromopenedcpannweightsfile=params;
            }
                  
           public int[] getparametersfromopenedcpannweightsfile(){
                    return parametersfromopenedcpannweightsfile;
            }
                        
            
            public boolean areTrainingParametersSaved(){
                    return trainingparameterssaved;
            }
            
           public void saveTrainingParameters(double[] arrayofparametersfortraining){
                trainingparameters=arrayofparametersfortraining;
                trainingparameterssaved=true;
            }
            
            public double[] getTrainingParameters(){
                    return trainingparameters;
            }
            
            
           public void setObjectsASCII(int objectsequencenumber,int asciicodeforobject){
                objectsASCIIcode[objectsequencenumber]=asciicodeforobject;
            }
            
            public int getObjectsASCII(int objectsequencenumber){
                    return objectsASCIIcode[objectsequencenumber];
            }                       
            
            public void setDescriptorsIncludedInTesting(int[] includeddescriptorsequencenumber){
                isdescriptorincludedintesting=includeddescriptorsequencenumber;
            }
            
            public int[] getDesriptorsIncludedInTesting(){
                    return isdescriptorincludedintesting;
            }
            
            public void setTextDelimiterUsed(String delimitertobeset){
                textdelimiterused=delimitertobeset;
            }
            
            public String getTextDelimiterUsed(){
                return textdelimiterused;
            }
            
                        
            public void saveSelectedDescriptorTarget(int descriptorsequencenumber,int selection){
                isdescriptortargetselected[descriptorsequencenumber]=selection;
            }
            public int isDescriptorTargetSelected(int descriptorsequencenumber){
                return isdescriptortargetselected[descriptorsequencenumber];
            }
            
            public void saveExictedNeuronPositionX(int objectsequencenumber,int posx){
                neuronXposition[objectsequencenumber]=posx;
            }
            public void saveExictedNeuronPositionY(int objectsequencenumber,int posy){
                neuronYposition[objectsequencenumber]=posy;
            }
            
            public int getExictedNeuronPositionX(int objectsequencenumber){
               return neuronXposition[objectsequencenumber];
            }
            
            public int getExictedNeuronPositionY(int objectsequencenumber){
               return neuronYposition[objectsequencenumber];
            }
                        
            public void setIfObjectIsInApplicabilityDomain(int objectsequencenumber,String isobjectindomain){
                objectisinapplicabilitydomain[objectsequencenumber]=isobjectindomain;
            }
            
            String getIfObjectIsInApplicabilityDomain(int objectsequencenumber){
                return objectisinapplicabilitydomain[objectsequencenumber];         
            }

            
            public void saveEuclideandistance(int objectsequencenumber,double eucliddistance){
                euclideandistance[objectsequencenumber]=eucliddistance;
            }
            
            public double getEuclideandistance(int objectsequencenumber){
                return euclideandistance[objectsequencenumber];
            } 
                    
            public void savePrediction(int objectsequencenumber,int targetnumber,double predictedvalue){
                targetpredictions[objectsequencenumber][targetnumber]=predictedvalue;
            }
            public double getPrediction(int objectsequencenumber,int targetnumber){
                return targetpredictions[objectsequencenumber][targetnumber];
            }  
                                    
            public void setObjectsIDs(int objectsequencenumber,int GivenIDToTheObject){
                objectID[objectsequencenumber]=GivenIDToTheObject;
            }
            
            public int getObjectsIDs(int objectinline){
                return objectID[objectinline];
            }                      
           
            public void setObjectDatasetNumber(int inlinenum,int datasetnumber){
                objectdatasetnumber[inlinenum]=datasetnumber;
            }
            
            public int getObjectDatasetNumber(int inlinenum){
                return objectdatasetnumber[inlinenum];
            }
 
            
            public void setNumberOfNeouronsInXDirection(int nx){
                numberofneuronsinXdirection=nx;
            }
            
            public int getNumberOfNeouronsInXDirection(){
                return numberofneuronsinXdirection;
            }
                        
            public void setNumberOfNeouronsInYDirection(int ny){
                numberofneuronsinYdirection=ny;
            }
            
            public int getNumberOfNeouronsInYDirection(){
                return numberofneuronsinYdirection;
            }
            
            public void setDescriptorOrTarget(int[] LineDefiningDescriptorOrTarget){
                descriptorortarget=LineDefiningDescriptorOrTarget;
            }
            
            public int[] checkIsDescriptorOrTarget(){
                return descriptorortarget;
            }
            
            public void saveNumberOfTargets(int inputnumberoftargets){
                numberoftargets=inputnumberoftargets;
                targetpredictions= new double[numberofobjects][numberoftargets];                               
                euclideandistance=new double[numberofobjects];
                objectisinapplicabilitydomain=new String[numberofobjects];
                neuronXposition=new int[numberofobjects];
                neuronYposition=new int[numberofobjects];                
                isdescriptorincludedintesting= new int[this.getNumberOfDescriptorsPlusTargets()-numberoftargets];
                
                for (int i=0;i<this.getNumberOfDescriptorsPlusTargets()-numberoftargets;i++){
                    isdescriptorincludedintesting[i]=1;
                }
                
                
            }
            
            public int getNumberOfTargets(){
                return numberoftargets;
            }
            
             public void saveNumberOfDescriptorsPlusTargets(int inputnumberofdescriptorsplustargets){
                numberofdescriptorsplustargets=inputnumberofdescriptorsplustargets;
                isdescriptortargetselected= new int[numberofdescriptorsplustargets];
            }
            
            public int getNumberOfDescriptorsPlusTargets(){
                return numberofdescriptorsplustargets;
            }
            

            public void saveNumberOfObjects(int inputnumberofobjects){
                numberofobjects=inputnumberofobjects;
                objectID= new int[numberofobjects];
                objectsASCIIcode= new int[numberofobjects]; 
                objectdatasetnumber= new int[numberofobjects];
            }
            
            public int getNumberOfObjects(){
                return numberofobjects;
            }
            
                    
            public void setinputdatavaluestobetested(double inputdataobjects[][]){
                mydatatobetested=inputdataobjects;
            }
            
            public void setcpannweights(double inputdataweights[][]){
                mycpannweights=inputdataweights;
            }
            
            public double[][] getinputdatavaluestobetested(){
                return mydatatobetested;
            }
            
            public double[][] getcpannweights(){
                return mycpannweights;
            }
            
          public void MyInputData() //constructor
            {
             /*
                mydatatobetested[0][0]=0;
                mycpannweights[0][0]=0;                
                numberofobjects=0;
                numberofdescriptorsplustargets=0;
                numberoftargets=0;
                descriptorortarget[0]=0;
                textdelimiterused=" ";
               */ 
                trainingparameterssaved=false;
                this.classcolorsinhashmap=new java.util.HashMap<>();
            }
  
            
            
         }