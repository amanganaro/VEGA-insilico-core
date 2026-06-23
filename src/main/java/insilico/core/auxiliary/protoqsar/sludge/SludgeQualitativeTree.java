package insilico.core.auxiliary.protoqsar.sludge;

/**
 *
 * @author Alberto
 */
public class SludgeQualitativeTree {
    
    
    public SludgeQualitativeTree() {
        
    }
    
    
//	Number of considered variables: 9
//	Dependent variable name="LDA"; location=1
//		 Category name="0"; numerical value=0
//		 Category name="1"; numerical value=1
//	Continuous predictor name="ATSC4p"; location=66
//	Continuous predictor name="ETA_BetaP_ns_d"; location=97
//	Continuous predictor name="GATS1i"; location=112
//	Continuous predictor name="GATS3c"; location=117
//	Continuous predictor name="maxHother"; location=195
//	Continuous predictor name="minsCH3"; location=231
//	Continuous predictor name="minwHBa"; location=240
//	Continuous predictor name="SpMax1_Bhm"; location=327

    public int Predict(double[] Descriptors) {
        
        // arrange descriptors to fit the input vector of the tree algorithm
        double[] inVector = new double[400];
        inVector[66] = Descriptors[0]; // ATSC4p
        inVector[97] = Descriptors[1]; // ETA_BetaP_ns_d
        inVector[112] = Descriptors[2]; // GATS1i
        inVector[117] = Descriptors[3]; // GATS3c
        inVector[195] = Descriptors[4]; // maxHother
        inVector[231] = Descriptors[5]; // minsCH3
        inVector[240] = Descriptors[6]; // minwHBa
        inVector[327] = Descriptors[7]; // SpMax1_Bhm
        
        return ApplyTree(inVector);
    }
    
    
    private int ApplyTree(double[] inVector) {
        
	double[] PredictProb = new double[2];
        double MaxValue = -1.0E30;
	int MaxVote = -1;


        PredictProb[0] = 0;

	if(  inVector[66] <= -2.48587244600000e+000 ) {
		PredictProb[0] = -1.27272727272727e+000;
	} 
	else if(  inVector[66] > -2.48587244600000e+000 ) {
		PredictProb[0] = 2.76679841897233e-001;
	} 
	if(  inVector[240] <= 1.97148841000000e-001 ) {
		PredictProb[0] += 6.14299541235383e-002;
	} 
	else if(  inVector[240] > 1.97148841000000e-001 ) {
		PredictProb[0] += -8.62733290003183e-002;
	} 
	if(  inVector[97] <= 1.01010000000000e-001 ) {
		PredictProb[0] += 1.96649662305734e-002;
	} 
	else if(  inVector[97] > 1.01010000000000e-001 ) {
		PredictProb[0] += -1.65199754577039e-001;
	} 
	if(  inVector[97] <= 3.70850000000000e-002 ) {
		PredictProb[0] += 3.91998467377920e-002;
	} 
	else if(  inVector[97] > 3.70850000000000e-002 ) {
		PredictProb[0] += -8.96041548021978e-002;
	} 
	if(  inVector[195] <= 3.82309010500000e-001 ) {
		PredictProb[0] += 4.44973033310427e-002;
	} 
	else if(  inVector[195] > 3.82309010500000e-001 ) {
		PredictProb[0] += -4.04805315309336e-002;
	} 
	if(  inVector[117] <= 1.06766938950000e+000 ) {
		PredictProb[0] += -4.00762109857524e-002;
	} 
	else if(  inVector[117] > 1.06766938950000e+000 ) {
		PredictProb[0] += 6.38316841264073e-002;
	} 
	if(  inVector[117] <= 1.12461456600000e+000 ) {
		PredictProb[0] += -6.46696722255448e-002;
	} 
	else if(  inVector[117] > 1.12461456600000e+000 ) {
		PredictProb[0] += 5.88150249977715e-002;
	} 
	if(  inVector[231] <= 1.44060185200000e+000 ) {
		PredictProb[0] += 6.29800786169196e-002;
	} 
	else if(  inVector[231] > 1.44060185200000e+000 ) {
		PredictProb[0] += -9.90388744274120e-002;
	} 
	if(  inVector[231] <= 1.88900462950000e+000 ) {
		PredictProb[0] += 3.81255920018023e-002;
	} 
	else if(  inVector[231] > 1.88900462950000e+000 ) {
		PredictProb[0] += -9.02941268398494e-002;
	} 
	if(  inVector[97] <= 9.54550000000000e-002 ) {
		PredictProb[0] += 2.65731737904522e-002;
	} 
	else if(  inVector[97] > 9.54550000000000e-002 ) {
		PredictProb[0] += -1.13758736659727e-001;
	} 
	if(  inVector[231] <= 2.20383893100000e+000 ) {
		PredictProb[0] += 3.72047560203168e-002;
	} 
	else if(  inVector[231] > 2.20383893100000e+000 ) {
		PredictProb[0] += -1.14800923380977e-001;
	} 
	if(  inVector[97] <= 3.25600000000000e-002 ) {
		PredictProb[0] += 3.86364880479052e-002;
	} 
	else if(  inVector[97] > 3.25600000000000e-002 ) {
		PredictProb[0] += -5.13074078499237e-002;
	} 
	if(  inVector[97] <= 1.05980000000000e-001 ) {
		PredictProb[0] += 9.99085776284301e-003;
	} 
	else if(  inVector[97] > 1.05980000000000e-001 ) {
		PredictProb[0] += -1.60760176373283e-001;
	} 
	if(  inVector[327] <= 6.87828787250000e+000 ) {
		PredictProb[0] += 1.38543547205007e-002;
	} 
	else if(  inVector[327] > 6.87828787250000e+000 ) {
		PredictProb[0] += -1.62812172149334e-001;
	} 
	if(  inVector[117] <= 7.08407613000000e-001 ) {
		PredictProb[0] += -1.69803200772819e-001;
	} 
	else if(  inVector[117] > 7.08407613000000e-001 ) {
		PredictProb[0] += -3.13569061552338e-003;
	} 
	if(  inVector[240] <= 4.62962950000000e-003 ) {
		PredictProb[0] += 8.98904723456177e-002;
	} 
	else if(  inVector[240] > 4.62962950000000e-003 ) {
		PredictProb[0] += -4.21760236571079e-002;
	} 
	if(  inVector[112] <= 7.73265194500000e-001 ) {
		PredictProb[0] += 2.84536876430503e-001;
	} 
	else if(  inVector[112] > 7.73265194500000e-001 ) {
		PredictProb[0] += 2.27975094078401e-003;
	} 
	if(  inVector[97] <= 3.45200000000000e-002 ) {
		PredictProb[0] += 4.19078702627758e-002;
	} 
	else if(  inVector[97] > 3.45200000000000e-002 ) {
		PredictProb[0] += -6.53516871819415e-002;
	} 
	if(  inVector[240] <= -2.26543840000000e-001 ) {
		PredictProb[0] += 6.84650072500122e-002;
	} 
	else if(  inVector[240] > -2.26543840000000e-001 ) {
		PredictProb[0] += -3.71214949203535e-002;
	} 
        
        if (MaxValue < PredictProb[0]) {
            MaxValue = PredictProb[0];
            MaxVote = 0;
        }
        
        PredictProb[1]=0;
                                
	if(  inVector[66] <= -2.02645500450000e+000 ) {
		PredictProb[1] = 1.04545454545455e+000;
	} 
	else if(  inVector[66] > -2.02645500450000e+000 ) {
		PredictProb[1] = -2.90404040404041e-001;
	} 
	if(  inVector[240] <= 8.63479990000000e-002 ) {
		PredictProb[1] += -4.57811007268905e-002;
	} 
	else if(  inVector[240] > 8.63479990000000e-002 ) {
		PredictProb[1] += 1.46202743001541e-001;
	} 
	if(  inVector[240] <= 1.39058642000000e-001 ) {
		PredictProb[1] += -3.95687922242646e-002;
	} 
	else if(  inVector[240] > 1.39058642000000e-001 ) {
		PredictProb[1] += 7.11724667447232e-002;
	} 
	if(  inVector[97] <= 1.05555000000000e-001 ) {
		PredictProb[1] += -2.42604538709456e-002;
	} 
	else if(  inVector[97] > 1.05555000000000e-001 ) {
		PredictProb[1] += 1.28830795562264e-001;
	} 
	if(  inVector[231] <= 1.93958105900000e+000 ) {
		PredictProb[1] += -3.56808683634254e-002;
	} 
	else if(  inVector[231] > 1.93958105900000e+000 ) {
		PredictProb[1] += 9.58510644960008e-002;
	} 
	if(  inVector[97] <= 1.18055000000000e-001 ) {
		PredictProb[1] += -2.26245363627605e-002;
	} 
	else if(  inVector[97] > 1.18055000000000e-001 ) {
		PredictProb[1] += 1.22204362461048e-001;
	} 
	if(  inVector[231] <= 1.99499817500000e+000 ) {
		PredictProb[1] += -5.04403943000005e-002;
	} 
	else if(  inVector[231] > 1.99499817500000e+000 ) {
		PredictProb[1] += 8.17367354590138e-002;
	} 
	if(  inVector[231] <= 1.40996256300000e+000 ) {
		PredictProb[1] += -5.74276586115099e-002;
	} 
	else if(  inVector[231] > 1.40996256300000e+000 ) {
		PredictProb[1] += 3.82066629787151e-002;
	} 
	if(  inVector[117] <= 8.65428170000000e-001 ) {
		PredictProb[1] += 7.87301601372848e-002;
	} 
	else if(  inVector[117] > 8.65428170000000e-001 ) {
		PredictProb[1] += -4.55794265349154e-002;
	} 
	if(  inVector[231] <= 1.94900463000000e+000 ) {
		PredictProb[1] += -2.86593662933286e-002;
	} 
	else if(  inVector[231] > 1.94900463000000e+000 ) {
		PredictProb[1] += 9.34616462234313e-002;
	} 
	if(  inVector[97] <= 1.02500000000000e-001 ) {
		PredictProb[1] += -1.80633606242264e-002;
	} 
	else if(  inVector[97] > 1.02500000000000e-001 ) {
		PredictProb[1] += 9.41909606212388e-002;
	} 
	if(  inVector[231] <= 1.92678013300000e+000 ) {
		PredictProb[1] += -3.92286310312384e-002;
	} 
	else if(  inVector[231] > 1.92678013300000e+000 ) {
		PredictProb[1] += 6.64365050931390e-002;
	} 
	if(  inVector[112] <= 8.16575804000000e-001 ) {
		PredictProb[1] += 1.07526440658286e-001;
	} 
	else if(  inVector[112] > 8.16575804000000e-001 ) {
		PredictProb[1] += -1.20756928570393e-002;
	} 
	if(  inVector[97] <= 3.45200000000000e-002 ) {
		PredictProb[1] += -1.78972913080524e-002;
	} 
	else if(  inVector[97] > 3.45200000000000e-002 ) {
		PredictProb[1] += 5.68872382089352e-002;
	} 
	if(  inVector[112] <= 8.46413586500000e-001 ) {
		PredictProb[1] += 8.41418777597809e-002;
	} 
	else if(  inVector[112] > 8.46413586500000e-001 ) {
		PredictProb[1] += -1.72105663329693e-002;
	} 
	if(  inVector[240] <= -4.79062455000000e-001 ) {
		PredictProb[1] += -8.91660206260044e-002;
	} 
	else if(  inVector[240] > -4.79062455000000e-001 ) {
		PredictProb[1] += 2.86393494094174e-002;
	} 
	if(  inVector[97] <= 3.70850000000000e-002 ) {
		PredictProb[1] += -5.67247443422804e-002;
	} 
	else if(  inVector[97] > 3.70850000000000e-002 ) {
		PredictProb[1] += 4.64162668409864e-002;
	} 
	if(  inVector[97] <= 1.18055000000000e-001 ) {
		PredictProb[1] += -2.74784919289977e-002;
	} 
	else if(  inVector[97] > 1.18055000000000e-001 ) {
		PredictProb[1] += 8.81501866915897e-002;
	} 
	if(  inVector[117] <= 1.06766938950000e+000 ) {
		PredictProb[1] += 4.91778360002413e-002;
	} 
	else if(  inVector[117] > 1.06766938950000e+000 ) {
		PredictProb[1] += -3.75260977525371e-002;
	} 
				
        if (MaxValue < PredictProb[1]) {
            MaxVote = 1;
        }
        
        return MaxVote;
        
    }
        
    
}