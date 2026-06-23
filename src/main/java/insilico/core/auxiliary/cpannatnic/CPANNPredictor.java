package insilico.core.auxiliary.cpannatnic;

import java.io.*;
import java.util.ArrayList;

/**
 * Class to run prediction for models created with CPANNatNIC version 1.01
 * 
 * @author Alberto Manganaro
 */
public class CPANNPredictor {
    
    private final double applicabilitydomainvalue=-1; // no AD by default?
    public CPANNInputData mydataclass;
    
    
    public CPANNPredictor() {
        mydataclass = new CPANNInputData();
    }
    
    
    private void updatemaindata(CPANNInputData tempdata,int whattoupdate){
        if (whattoupdate==0) //update values to be tested
            mydataclass.setinputdatavaluestobetested(tempdata.getinputdatavaluestobetested());

        if (whattoupdate==1) //update cpann weights
            mydataclass.setcpannweights(tempdata.getcpannweights());
    }
    

    
//////// methods for loading user data (dataset with descriptors
          
    public void LoadSingleCompound(double[] Descriptors, String[] DescNames) {

        mydataclass.saveNumberOfDescriptorsPlusTargets(Descriptors.length + 1);
        mydataclass.saveNumberOfObjects(1);
        mydataclass.saveNumberOfTargets(1);
        mydataclass.saveAllVariableNames(DescNames);
               
        int[] descriptorortarget=new int[Descriptors.length + 1];
        for (int dt=0;dt<Descriptors.length;dt++)
        {
            descriptorortarget[dt] = 1;
            mydataclass.saveSelectedDescriptorTarget(dt,1);
        }
        mydataclass.saveSelectedDescriptorTarget(Descriptors.length,1);
        mydataclass.setDescriptorOrTarget(descriptorortarget);
        
        mydataclass.setObjectsIDs(0, 1);
        mydataclass.setObjectsASCII(0, 1);
        mydataclass.setObjectDatasetNumber(0, 1);
        
        double[][] dataarray = new double[1][Descriptors.length + 1];
        for (int dt=0;dt<Descriptors.length;dt++)
            dataarray[0][dt] = Descriptors[dt];
        dataarray[0][Descriptors.length] = 0;
        
        CPANNInputData mydata = new CPANNInputData();
        mydata.setinputdatavaluestobetested(dataarray);
        
        updatemaindata(mydata,0);
    }    
    
    
    
//////// methods for loading CPANN weights and settings    
    
    public void LoadCPANNObject(InputStream SrcFile) throws Exception {
                      
        String line;
        int numberofobjects=0;
        int numberofodesplustar=0;
        int nx;
        int ny;
        double[][] dataarray;
        dataarray = new double[numberofobjects][numberofodesplustar];
        int counter = 0;
        int formatlength=0;
        String textdelimiter;
        textdelimiter=" ";
        BufferedReader bufferedReader;
        int typeofdistance=-1;
        int ismodeltoroidal=-1;
        int normalizationfactorspresent=0;
        int[] cpannparametersinopenedfile=new int[5];
                
        try {

            DataInputStream in = new DataInputStream(SrcFile);
            bufferedReader =  new BufferedReader(new InputStreamReader(in));
                        
            while((line = bufferedReader.readLine()) != null) {
                String[] tempstring2;
                
                if (counter==1)
                {

                    tempstring2=line.substring(1,12).split(textdelimiter,-1);
                    nx=Integer.parseInt(tempstring2[tempstring2.length-1]);//check if nx is the first one
                    
                    tempstring2=line.substring(13,24).split(textdelimiter,-1);
                    ny=Integer.parseInt(tempstring2[tempstring2.length-1]);//check if ny is the second one 
                    
                    numberofobjects=nx*ny;
                    
                    tempstring2=line.substring(25,36).split(textdelimiter,-1);
                    numberofodesplustar=Integer.parseInt(tempstring2[tempstring2.length-1]);
                    
                    tempstring2=line.substring(37,48).split(textdelimiter,-1);
                    typeofdistance=Integer.parseInt(tempstring2[tempstring2.length-1]);

                   
                    tempstring2=line.substring(49,60).split(textdelimiter,-1);
                    ismodeltoroidal=Integer.parseInt(tempstring2[tempstring2.length-1]);
                    
                    tempstring2=line.substring(61,72).split(textdelimiter,-1);
                    normalizationfactorspresent=Integer.parseInt(tempstring2[tempstring2.length-1]);                    

                    cpannparametersinopenedfile[0]=nx;
                    cpannparametersinopenedfile[1]=ny;
                    cpannparametersinopenedfile[2]=numberofodesplustar;
                    cpannparametersinopenedfile[3]=typeofdistance;
                    cpannparametersinopenedfile[4]=ismodeltoroidal;
                    
                    mydataclass.savecpannparametersinopenedfile(cpannparametersinopenedfile);
                    mydataclass.saveNumberOfDescriptorsPlusTargets(numberofodesplustar);
                    mydataclass.saveNumberOfTargets(1);
                            
                    dataarray = new double[numberofobjects][numberofodesplustar];
                    
                    mydataclass.setNumberOfNeouronsInXDirection(nx);
                    mydataclass.setNumberOfNeouronsInYDirection(ny);
                            
                }else if(counter==2){
                    //get variable names from the selected model file

                    if (line.split(textdelimiter,-1).length<mydataclass.getNumberOfDescriptorsPlusTargets()){
                         System.out.println("Model varible names missing!");
                     String[] tempmodelvariablenames=new String[mydataclass.getNumberOfDescriptorsPlusTargets()];
                        for (int dt=0;dt<mydataclass.getNumberOfDescriptorsPlusTargets();dt++)
                            {
                                tempmodelvariablenames[dt]="var"+String.valueOf(dt+1);
                         }
                         mydataclass.saveAllModelVariableNames(tempmodelvariablenames);
                    }
       
                    String[] tempmodelvariablenames=line.split(textdelimiter,-1);
                    mydataclass.saveAllModelVariableNames(tempmodelvariablenames);                  
                    String testmodelvarnamestring =mydataclass.getAllModelVariableNames()[mydataclass.getNumberOfDescriptorsPlusTargets()-1];
                    
                   //end saving model variable names
                    
                    line = bufferedReader.readLine();//continue reading next line
                    formatlength=(line.length())/(mydataclass.getNumberOfDescriptorsPlusTargets());
                    //check input in 3rd line
                    double tempsum=0;
                    for (int pp=1;pp<(line.length())/formatlength;pp++){
                        tempstring2=line.substring((pp-1)*formatlength+1,pp*formatlength).split(textdelimiter,-1);                       
                        tempsum=tempsum+Double.parseDouble(tempstring2[tempstring2.length-1]);
                    }
                                        
                    if((int)tempsum!=(mydataclass.getNumberOfDescriptorsPlusTargets()-mydataclass.getNumberOfTargets())){
                        throw new Exception("Selected model has different number of descriptors as the file for testing");
                    }
                    
                }
                
                    if (normalizationfactorspresent==1){
                       if (counter==3){ 
                    int numdes=mydataclass.getNumberOfDescriptorsPlusTargets()-mydataclass.getNumberOfTargets();
                    double[] calculatednormalizationmean =new double[numdes];
                     for (int i=1;i<=numdes;i++)
                    {
                    tempstring2=line.substring((i-1)*formatlength+1,i*formatlength).split(textdelimiter,-1);//two formats of unw files: lenggth 15 or 10 characters for a number
                    calculatednormalizationmean[i-1]=Double.parseDouble(tempstring2[tempstring2.length-1]);
                     }
                   this.mydataclass.saveNormalizationMean(calculatednormalizationmean);
                    }                    
                      if(counter==4){
                    int numdes=mydataclass.getNumberOfDescriptorsPlusTargets()-mydataclass.getNumberOfTargets();
                    double[] calculatednormalizationstandarddeviations =new double[numdes];
                     for (int i=1;i<=numdes;i++)
                    {
                    tempstring2=line.substring((i-1)*formatlength+1,i*formatlength).split(textdelimiter,-1);//two formats of unw files: lenggth 15 or 10 characters for a number
                    calculatednormalizationstandarddeviations[i-1]=Double.parseDouble(tempstring2[tempstring2.length-1]);
                     }
                   this.mydataclass.saveNormalizationStandardDeviation(calculatednormalizationstandarddeviations);                 
                   this.mydataclass.setNormalizationDataSaved(true);

                }
                       
                    }else{
                        counter=counter;
                    }

                    if (normalizationfactorspresent==0){
                        if (counter>=3){
                    for (int i=1;i<=numberofodesplustar;i++)
                    {
                        
                      tempstring2=line.substring((i-1)*formatlength+1,i*formatlength).split(textdelimiter,-1);//two formats of unw files: lenggth 15 or 10 characters for a number

                     dataarray[counter-3][i-1]=Double.parseDouble(tempstring2[tempstring2.length-1]);
                    }
                    }
                    }
                                            
                    

                    if (normalizationfactorspresent==1){
                      if (counter>=5){
                    for (int i=1;i<=numberofodesplustar;i++)
                    {
                        
                      tempstring2=line.substring((i-1)*formatlength+1,i*formatlength).split(textdelimiter,-1);//two formats of unw files: lenggth 15 or 10 characters for a number


                     dataarray[counter-5][i-1]=Double.parseDouble(tempstring2[tempstring2.length-1]);

                    }
                    }
                    
                }

                counter =counter+1;
            }	

            bufferedReader.close();
            
        } catch(NumberFormatException nfe){
            throw new Exception(nfe.getMessage());
        } catch(FileNotFoundException ex) {
            throw new Exception("Unable to open file - " + ex.getMessage());
        } catch(IOException ex) {
            throw new Exception("Error reading file - " + ex.getMessage());
        } catch(Exception ex) {
            throw new Exception("General error - " + ex.getMessage());
        }  

        CPANNInputData mydata = new CPANNInputData();
        mydata.setcpannweights(dataarray);
        
        updatemaindata(mydata,1);                    
    }            
    
    
    
//////// methods to run prediction with loaded data    
    
    public ArrayList<CPANNResults> PerformPrediction() {                                                  
        
        // settare descrittori???

        ArrayList<CPANNResults> Results = new ArrayList<>();
        
        try{
            double[][] cpannweights=mydataclass.getcpannweights();
            double[][] objects=mydataclass.getinputdatavaluestobetested();
            int numberofobj=mydataclass.getNumberOfObjects();
            int nx=mydataclass.getNumberOfNeouronsInXDirection();
            int ny=mydataclass.getNumberOfNeouronsInYDirection();
            int numberoftargets=mydataclass.getNumberOfTargets();
            double tempdistance=0;
            double tempdistanceproductxiyi=0;//for Tanimoto coefficient

            int numberofneurons=nx*ny;
            int numberofdescriptors=mydataclass.getNumberOfDescriptorsPlusTargets()-mydataclass.getNumberOfTargets();


            int[] descriptorselectedfortesting=mydataclass.getDesriptorsIncludedInTesting();

            double[][] euclideandistance= new double[numberofobj][numberofneurons];
            double[] mineuclideandistance = new double[numberofobj];
            int[] neuronexcitedbytheobject = new int[numberofobj];

            double[] tempRMS=new double[mydataclass.getNumberOfTargets()];
            for (int ita=0;ita<mydataclass.getNumberOfTargets();ita++){
                tempRMS[ita]=0;
            }

            double[][] exptargetvalues=new double[mydataclass.getNumberOfObjects()][mydataclass.getNumberOfTargets()];
            double[][] predictedvalues=new double[mydataclass.getNumberOfObjects()][mydataclass.getNumberOfTargets()];

            int[] parametersfromopenedcpannfile=this.mydataclass.getparametersfromopenedcpannweightsfile();

            for(int objectid=0;objectid<numberofobj;objectid++){
                
                CPANNResults curResult = new CPANNResults();
                
                for (int neuronid=0;neuronid<numberofneurons;neuronid++){
                    tempdistance=0;
                    tempdistanceproductxiyi=0;

                    for (int descriptorid=0;descriptorid<numberofdescriptors;descriptorid++)
                    {
                        if(descriptorselectedfortesting[descriptorid]==1){
                            if (parametersfromopenedcpannfile[3]==2){
                                tempdistance=tempdistance+Math.pow(objects[objectid][descriptorid]-cpannweights[neuronid][descriptorid],2);
                            }else if((parametersfromopenedcpannfile[3]==1)){
                                tempdistance=tempdistance+objects[objectid][descriptorid]*cpannweights[neuronid][descriptorid];
                            }else if((parametersfromopenedcpannfile[3]==3)){
                                tempdistance=tempdistance+Math.pow(objects[objectid][descriptorid]-cpannweights[neuronid][descriptorid],2);
                                tempdistanceproductxiyi=tempdistanceproductxiyi+objects[objectid][descriptorid]*cpannweights[neuronid][descriptorid];
                            }
                        }
                    }
                    euclideandistance[objectid][neuronid]=Math.pow(tempdistance, 0.5);

                    if((parametersfromopenedcpannfile[3]==3)){//for Tanimoto distance
                        euclideandistance[objectid][neuronid]=tempdistanceproductxiyi/(tempdistance+tempdistanceproductxiyi);
                    }
                }

                mineuclideandistance[objectid]=euclideandistance[objectid][0];
                for (int neuronid=0;neuronid<numberofneurons;neuronid++)
                {
                    mineuclideandistance[objectid]=Math.min(mineuclideandistance[objectid],euclideandistance[objectid][neuronid]);
                    if (mineuclideandistance[objectid]==euclideandistance[objectid][neuronid])
                    {
                        //selected neuron for the object with objectid
                        neuronexcitedbytheobject[objectid]=neuronid;
                        curResult.setNeuron(neuronid);
                    }
                }

                curResult.setDistance(mineuclideandistance[objectid]);
                
                int onepasswehennotargets=0;
                if (numberoftargets==0){
                    onepasswehennotargets=1;
                }

                for (int targetid=0;targetid<numberoftargets+onepasswehennotargets;targetid++){

                    int posy=(int)(Math.ceil(((double)(neuronexcitedbytheobject[objectid]+1))/((double)nx)));
                    int posx=neuronexcitedbytheobject[objectid]+1-(posy-1)*nx;

                    mydataclass.saveEuclideandistance(objectid, mineuclideandistance[objectid]);
                    mydataclass.saveExictedNeuronPositionX(objectid, posx);
                    mydataclass.saveExictedNeuronPositionY(objectid, posy);

                    if (this.applicabilitydomainvalue>=0){
                        if (mineuclideandistance[objectid]<=this.applicabilitydomainvalue){
                            mydataclass.setIfObjectIsInApplicabilityDomain(objectid,"yes");
                        }else{
                            mydataclass.setIfObjectIsInApplicabilityDomain(objectid,"no");
                        }
                    }else{
                        mydataclass.setIfObjectIsInApplicabilityDomain(objectid,"undefined");
                    }

                    if (numberoftargets>0){
                        mydataclass.savePrediction(objectid, targetid, cpannweights[neuronexcitedbytheobject[objectid]][mydataclass.getNumberOfDescriptorsPlusTargets()-mydataclass.getNumberOfTargets()+targetid]);
                        predictedvalues[objectid][targetid]=cpannweights[neuronexcitedbytheobject[objectid]][mydataclass.getNumberOfDescriptorsPlusTargets()-mydataclass.getNumberOfTargets()+targetid];
                        curResult.setPrediction(predictedvalues[objectid][targetid]);
                    }

                    if (this.mydataclass.getNumberOfTargets()>0){

                        double[][] temptestdata=this.mydataclass.getinputdatavaluestobetested();
                        double temppred1=cpannweights[neuronexcitedbytheobject[objectid]][mydataclass.getNumberOfDescriptorsPlusTargets()-mydataclass.getNumberOfTargets()+targetid];
                        double tempexper1=temptestdata[objectid][mydataclass.getNumberOfDescriptorsPlusTargets()-mydataclass.getNumberOfTargets()+targetid];
                        tempRMS[targetid]=tempRMS[targetid]+(temppred1-tempexper1)*(temppred1-tempexper1);

                        exptargetvalues[objectid][targetid]=tempexper1;

                     }
                    
                }
                
                curResult.setIsValid(true);
                Results.add(curResult);
                
            }

        } catch(ArrayIndexOutOfBoundsException ex) {
            System.out.println("Index out of bounds! Check your input data!");
        }

        return(Results);
    }    
      
}
