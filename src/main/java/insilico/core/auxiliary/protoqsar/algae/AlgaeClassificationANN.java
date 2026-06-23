package insilico.core.auxiliary.protoqsar.algae;

/**
 *
 * @author Alberto
 */
public class AlgaeClassificationANN {
    
    private final static double[][] INPUT_HIDDEN_WEIGHTS =
		{
		 {4.79083114370771e-001, -9.51595437702049e-001, -6.55493574640823e-001, 1.13067821568573e+000, -1.54147421186796e+000, 7.07011435870248e-001, -6.85679100554919e-002, 2.09224710807161e-001, 8.15354984036620e-001, -5.05641513599652e-001, 8.18206953257293e-001, -1.53090874224750e+000, -8.59256194014857e-001, -1.85445437495411e-001 },
		 {1.45395512979970e+000, 1.02695163200900e+000, 4.37603905724007e-001, -1.57350657432253e+000, -1.36745709842730e+000, 6.17022187471969e-001, 8.83466532563357e-001, 1.27880333259879e+000, -8.14277725467656e-001, -2.15482411760109e+000, -9.98499727508709e-001, 1.15919373888621e+000, 2.40895047609831e+000, 4.57431074076560e-001 },
		 {2.86140623888411e+000, 2.91290368536025e+000, 1.28222332284511e-001, -2.13634688484343e-001, -1.15096126188007e+000, 5.73815021177147e-001, 2.82094091592286e+000, 3.87925324286238e-001, 1.23840081553625e-001, -1.93728885923877e+000, -4.18396338278246e-001, -4.73185536525787e-002, 9.62372439521680e-001, 3.90681303135955e-001 },
		 {-2.68623429435504e-001, -2.54019542908536e-001, 8.71946035847739e-002, -3.19619317180021e-001, 6.99760702750380e-001, -3.95870009385030e-001, 3.63857960200440e-001, 1.30429562361805e-001, -3.40700635175097e-001, -6.51588117329271e-002, 2.17380810589337e-001, 3.72170815369395e-001, 4.30724179250760e-001, 3.52372215969454e-001 },
		 {5.56485460215431e-002, 1.75357680897436e+000, -4.35810715738208e-001, 7.52737293333651e-001, 7.35103538577692e-001, -8.61668432287649e-002, 1.01406865690151e+000, -1.15332080262282e+000, -4.73350948030616e-001, 1.61898024135833e+000, 9.90363225492310e-001, 5.21505087052729e-001, -5.63636484470825e-001, -1.06999404255343e+000 } 
		};
    private final static double[] HIDDEN_BIAS = { -8.03741467721854e-001, -1.06141464098335e+000, -8.59981130488585e-001, -4.03723561508911e-001, 8.31331976260293e-001 };
    private final static double[][] HIDDEN_OUTPUT_WTS = 
		{
		 {8.89959595100345e-001, -1.22465463212511e+000, -4.36554933307008e-001, -2.67726733456564e-001, 5.20138255738294e-001 },
		 {-4.22027168347374e-001, 5.04590482696022e-001, 9.07074483697110e-001, 1.82852083910168e-001, -1.27095982567680e+000 }
		};
    private final static double[] OUTPUT_BIAS = { -9.49239621211264e-001, -3.53609813410571e-001 };
    private final static double[] MAX_INPUT = { 5.65500000000000e+000, 1.00000000000000e+000, 1.00000000000000e+000, 1.00000000000000e+000, 1.00000000000000e+000, 1.00000000000000e+000, 1.00000000000000e+000, 1.00000000000000e+000, 1.00000000000000e+000, 1.20000000000000e+001, 1.20000000000000e+001, 6.00000000000000e+000, 6.00000000000000e+000, 5.00000000000000e-001 };
    private final static double[] MIN_INPUT = { 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000, 0.00000000000000e+000 };
           
    public int ResultClass;
    public double ResultConfidence;
    
    
    public AlgaeClassificationANN() {
        ResultClass = -1;
        ResultConfidence = 0;
    }
    
    
    private double[] ScaleInputs(double[] input, double minimum, double maximum, int size) {
        
        double delta;
        double[] scaled_input = new double[input.length];
        for(int i=0; i<size; i++)
        {
            delta = (maximum-minimum)/(MAX_INPUT[i]-MIN_INPUT[i]);
            scaled_input[i] = minimum - delta*MIN_INPUT[i]+ delta*input[i];
        }
        return scaled_input;
    }
    
    
//    private int FindMax(double[] vec, int len) {
//        int maxIndex = 0;        
//        for (int i=0; i<len; i++) {
//            if (vec[i] > vec[maxIndex]) {
//                maxIndex = i;
//            }
//        }
//        return maxIndex;
//    }
//    
//    
//    private double[] softmax(double[] vec, int len) {
//
//        double[] new_vec = new double[vec.length];
//        for (int i=0; i<vec.length; i++)
//            new_vec[i] = vec[i];
//        
//        double sum=0.0;
//        for(int i=0; i<len; i++) {
//            if(new_vec[i]>200) {
//                int maxIndex = FindMax(new_vec, len);
//                double max = new_vec[maxIndex];
//                for(int j=0; j<len; j++) {
//                    if(j==maxIndex) new_vec[j] = 1.0;
//                    else new_vec[j] = 0.0;
//                }
//                return new_vec;
//            } else {
//                new_vec[i] = Math.exp(new_vec[i]);
//            }
//                sum += new_vec[i];
//        }
//  
//        if(sum!=0.0) {
//            for(int i=0; i<len; i++)
//                new_vec[i] = new_vec[i]/sum;
//        } else for(int i=0; i<len; i++) 
//            new_vec[i] = 1.0/(double)len;    
//        
//        return new_vec;
//    }
//    
//
//    private double logistic(double x) {
//        if(x > 100.0) x = 1.0;
//        else if (x < -100.0) x = 0.0;
//        else x = 1.0/(1.0 + Math.exp(-x));
//        return x;
//    }    

    
    private double[] ComputeFeedForwardSignals(double[][] MAT_INOUT, double[] V_IN, 
            double[] V_BIAS, int size1, int size2, int layer) {
        
        double[] V_OUT = new double[size2];
        for(int row=0; row < size2; row++) {
            V_OUT[row]=0.0;
            
            for(int col=0;col<size1;col++) {
                V_OUT[row] += MAT_INOUT[row][col] * V_IN[col];
            }
            
            V_OUT[row] += V_BIAS[row];

            if(layer==0) 
                V_OUT[row] = Math.tanh(V_OUT[row]);
            if(layer==1) 
                V_OUT[row] = Math.exp(V_OUT[row]);
            
            
        }
        
        return V_OUT;
    }

    
    //
    // input: double vector with 14 molecular descriptors 
    // ATS5m
    // B01[C-Cl]
    // B01[C-O]
    // B02[Cl-Cl]
    // B02[N-O]
    // B03[N-O]
    // B09[C-C]
    // B09[O-O]
    // B10[C-O]
    // F01[C-O]
    // F02[C-O]
    // F04[O-O]
    // F10[C-N]
    // X3A    
    //
    public void RunPrediction(double[] input) {

        double max = 3.e-300;
        double[] scaled_inputs = ScaleInputs(input,0,1,14);
        double[] hidden = ComputeFeedForwardSignals(INPUT_HIDDEN_WEIGHTS,scaled_inputs,HIDDEN_BIAS,14, 5,0);
        double[] output = ComputeFeedForwardSignals(HIDDEN_OUTPUT_WTS,hidden,OUTPUT_BIAS,5, 2,1);

        ResultClass = -1;
        for(int i=0;i<2;i++) {
            if(max<output[i]) {
                max = output[i];
                ResultClass = i;
            }
        }
        
        ResultConfidence = max;
    }
    

}


