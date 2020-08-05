package insilico.core.similarity;

import insilico.core.descriptor.Descriptor;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.BitSet;

/**
 * Calculation of similarity index. Constructor sets parameters to the default
 * values, parameters can be anyway modified.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class Similarity implements Serializable {

    Logger logger = LoggerFactory.getLogger(Similarity.class);
    
    private static final long serialVersionUID = 1L;

    // Similarity's weights
    private double WeightConstitutional;
    private double WeightHetero;
    private double WeightFunctionalGroups;
    private double WeightFP;

    // Distance for non binary keys
    private int NonBinaryDistanceType;
    public final static int DISTANCE_CAMBERRA = 1;
    public final static int DISTANCE_DIVERGENCE = 2;
    public final static int DISTANCE_BRAY_CURTIS = 3;
    public final static int DISTANCE_DICE = 4;
    public final static int DISTANCE_SOKAL_SNEATH = 5;
    public final static int DISTANCE_COSINE = 6;

    // Distance type fon binary keys
    private int BinaryDistanceType;
    public final static int DISTANCE_MAXWELL_PILLINER = 37;

    /**
     * Constructor
     */
    public Similarity() {

        // Default weights
        WeightFP                = 0.4;
        // Constitutional original weight = 0.3
        WeightConstitutional    = 0.35;
        WeightHetero            = 0.1;
        WeightFunctionalGroups  = 0.15;

        // Default similarity coefficients
        NonBinaryDistanceType = DISTANCE_BRAY_CURTIS;
        BinaryDistanceType = DISTANCE_MAXWELL_PILLINER;
    }

    // GETTERS AND SETTERS
    /**
     * @return the WeightConstitutional
     */
    public double getWeightConstitutional() {
        return WeightConstitutional;
    }


    /**
     * @param WeightConstitutional the WeightConstitutional to set
     */
    public void setWeightConstitutional(double WeightConstitutional) {
        this.WeightConstitutional = WeightConstitutional;
    }


    /**
     * @return the WeightHetero
     */
    public double getWeightHetero() {
        return WeightHetero;
    }


    /**
     * @param WeightHetero the WeightHetero to set
     */
    public void setWeightHetero(double WeightHetero) {
        this.WeightHetero = WeightHetero;
    }


    /**
     * @return the WeightFunctionalGroups
     */
    public double getWeightFunctionalGroups() {
        return WeightFunctionalGroups;
    }


    /**
     * @param WeightFunctionalGroups the WeightFunctionalGroups to set
     */
    public void setWeightFunctionalGroups(double WeightFunctionalGroups) {
        this.WeightFunctionalGroups = WeightFunctionalGroups;
    }


    /**
     * @return the WeightFP
     */
    public double getWeightFP() {
        return WeightFP;
    }


    /**
     * @param WeightFP the WeightFP to set
     */
    public void setWeightFP(double WeightFP) {
        this.WeightFP = WeightFP;
    }


    /**
     * @return the NonBinaryDistanceType
     */
    public int getNonBinaryDistanceType() {
        return NonBinaryDistanceType;
    }


    /**
     * @param NonBinaryDistanceType the NonBinaryDistanceType to set
     */
    public void setNonBinaryDistanceType(int NonBinaryDistanceType) {
        this.NonBinaryDistanceType = NonBinaryDistanceType;
    }


    /**
     * @return the BinaryDistanceType
     */
    public int getBinaryDistanceType() {
        return BinaryDistanceType;
    }


    /**
     * @param BinaryDistanceType the BinaryDistanceType to set
     */
    public void setBinaryDistanceType(int BinaryDistanceType) {
        this.BinaryDistanceType = BinaryDistanceType;
    }

    // METHODS AND FUNCTIONS
    /**
     * Calculates similarity index given the descriptors of two molecules.
     *
     * @param DescMolA descriptors of first molecule
     * @param DescMolB descriptors of second molecule
     * @return similarity index value
     */
    public double Calculate(SimilarityDescriptors DescMolA, SimilarityDescriptors DescMolB) {

        double sim_const;
        double sim_hetero;
        double sim_functgroups;
        double sim_FP;

        // Calculate contributions
        sim_const = getNonBinaryDistance(DescMolA.Constitutional, DescMolB.Constitutional, getNonBinaryDistanceType());
        sim_hetero = getNonBinaryDistance(DescMolA.HeteroAtoms, DescMolB.HeteroAtoms, getNonBinaryDistanceType());
        sim_functgroups = getNonBinaryDistance(DescMolA.FunctionalGroups, DescMolB.FunctionalGroups, getNonBinaryDistanceType());
        sim_FP = getBinaryDistance(DescMolA.Fingerprint, DescMolB.Fingerprint, getBinaryDistanceType());

        // Check for wrong values due to missing values, if found is set to 1 to be not evaluated in the final formula
        if ( (HasMissingValues(DescMolA.Constitutional)) || (HasMissingValues(DescMolB.Constitutional)) )
            sim_const = 1;
        if ( (HasMissingValues(DescMolA.HeteroAtoms)) || (HasMissingValues(DescMolB.HeteroAtoms)) )
            sim_hetero = 1;
        if ( (HasMissingValues(DescMolA.FunctionalGroups)) || (HasMissingValues(DescMolB.FunctionalGroups)) )
            sim_functgroups = 1;

        // Calculates similarity and truncates result to 3 decimal digits precision
        double sim = Math.pow(sim_FP, getWeightFP()) * Math.pow(sim_const, getWeightConstitutional())
                * Math.pow(sim_hetero, getWeightHetero()) * Math.pow(sim_functgroups, getWeightFunctionalGroups());
        sim = Math.floor(sim*1000) * 0.001;

        return sim;
    }


    /**
     * Calculates similarity index given the descriptors of two molecules, and
     * checks for false exact matchings. If two molecules have similarity equal
     * to 1 but they are not exactly the same, a similarity of 0.999 is returned.
     *
     * @param DescMolA descriptors of first molecule
     * @param DescMolB descriptors of second molecule
     * @param StructureA structure of the first molecule
     * @param StructureB structure of the second molecule
     * @return similarity index value
     */
    public double CalculateExactMatches(SimilarityDescriptors DescMolA, SimilarityDescriptors DescMolB,
                                        IAtomContainer StructureA, String StructureB){

        double sim = Calculate(DescMolA, DescMolB);

        if (sim == 1){
            InsilicoMolecule mol = SmilesMolecule.Convert(StructureB);
            try {
                if (!(CheckIsomorphism(StructureA, mol.GetStructure()))){
                    sim = 0.99;
                }
            } catch (InvalidMoleculeException e) {
                e.printStackTrace();
                logger.warn("unable to check exact similarity for molecule " + mol.GetSMILES());
            }
        }

        return sim;
    }


    /**
     * Checks if at least a value in an array is a missing value (-999).
     * @param vector array of descriptors
     * @return true if at least one value is a missing value (-999)
     */
    private static boolean HasMissingValues(double[] vector) {
        for (double v : vector) {
            if (v == Descriptor.MISSING_VALUE)
                return true;
        }
        return false;
    }

    /**
     * Checks if two molecules (given as CDK IMolecule objects) have
     * exactly the same structure. It uses the UniversalIsomorphismTester
     * from CDK.
     *
     * @param mol1 first molecule
     * @param mol2 second molecule
     * @return true if A and B are isomorph
     */
    public static boolean CheckIsomorphism(IAtomContainer mol1, IAtomContainer mol2) {

        boolean res;

        if ((mol1==null)||(mol2==null))
            return false;

        try {
            UniversalIsomorphismTester isomorphismTester = new UniversalIsomorphismTester();
            res = isomorphismTester.isIsomorph(mol1, mol2);
        } catch (Exception e) {
            res = false;
        }

        return res;
    }

    /**
     * Calculates a distance coefficient for non-binary keys.
     *
     * @param fpA first key (array of descriptors)
     * @param fpB second key (array of descriptors)
     * @param DistanceType type of coefficient to be used
     * @return value of the distance
     */
    private static double getNonBinaryDistance(double[] fpA, double[] fpB, int DistanceType) {
        double s = 0;
        double sumTot = 0;
        double diff = 0;
        double sum = 0;
        double prod = 0;
        double a = 0;
        double b = 0;
        boolean AllZerosfpA = true, AllZerosfpB = true;

        switch (DistanceType) {
            case DISTANCE_CAMBERRA:
                //mean camberra
                for (int i = 0; i < fpA.length; i++){
                    diff += Math.abs( fpA[i] - fpB[i] );
                    sum += Math.abs( fpA[i] + fpB[i] );
                    sumTot += ( diff / sum );
                }
                s = 1 - ( sumTot / fpA.length );
                break;
            case DISTANCE_DIVERGENCE:
                //divergence
                for (int i = 0; i < fpA.length; i++){
                    diff += Math.abs( fpA[i] - fpB[i] );
                    sum += Math.abs( fpA[i] + fpB[i] );
                    sumTot += Math.pow( (diff / sum), 2 );
                }
                s = 1 - Math.sqrt( sumTot / fpA.length );
                break;
            case DISTANCE_BRAY_CURTIS:
                //bray curtis
                for (int i = 0; i < fpA.length; i++){
                    if (fpA[i] != 0)
                        AllZerosfpA = false;
                    if (fpB[i] != 0)
                        AllZerosfpB = false;

                    diff += Math.abs( fpA[i] - fpB[i] );
                    sum += ( fpA[i] + fpB[i] );
                }
                if ((AllZerosfpA)&&(AllZerosfpB)) {
                    // both keys all set to zero
                    // default return value: 1
                    s = 1;
                } else {
                    if ( ((AllZerosfpA)||(AllZerosfpB)) || (sum==diff)) {
                        // if one of the keys is all set to zero
                        // or in general if sum = diff (maximum
                        // theoretical difference between keys)
                        // add a correction of +1 to sum
                        sum++;
                    }
                    s = 1 - ( diff / sum );
                }
                break;
            case DISTANCE_DICE:
                //dice
                for (int i = 0; i < fpA.length; i++){
                    prod += ( fpA[i] * fpB[i] );
                    a += Math.pow( fpA[i], 2 );
                    b += Math.pow( fpB[i], 2 );
                }
                s = ( 2 * prod ) / ( a + b );
                break;
            case DISTANCE_SOKAL_SNEATH:
                // Sokal/sneath
                for (int i = 0; i < fpA.length; i++){
                    prod += ( fpA[i] * fpB[i] );
                    a += Math.pow( fpA[i], 2 );
                    b += Math.pow( fpB[i], 2 );
                }
                s = ( prod ) / ( (2*a) + (2*b) - (3 * prod) );
                break;
            case DISTANCE_COSINE:
                // cosine/ochiai
                for (int i = 0; i < fpA.length; i++){
                    prod += ( fpA[i] * fpB[i] );
                    a += Math.pow( fpA[i], 2 );
                    b += Math.pow( fpB[i], 2 );
                }
                s = ( prod ) / Math.sqrt( a * b );
                break;
            default:
                s = 0;
        }

        return s;
    }

    /**
     * Calculates a distance coefficient for binary keys (bit sets).
     *
     * @param fpA first key (bit set)
     * @param fpB second key (bit set)
     * @param DistanceType type of coefficient to be used
     * @return value of the distance
     */
    private static double getBinaryDistance(BitSet fpA, BitSet fpB, int DistanceType) {

        int a=0, b=0, c=0, d=0;

        for (int i=0; i<fpA.size(); i++) {
            if ( (fpA.get(i)) && (fpB.get(i)) )
                a++;
            else if ( (fpA.get(i)) && (!fpB.get(i)) )
                b++;
            else if ( (!fpA.get(i)) && (fpB.get(i)) )
                c++;
            else if ( (!fpA.get(i)) && (!fpB.get(i)) )
                d++;
        }

        double p = a + b + c + d;
        double Q = (a + b)*(a + c)*(b + d)*(c + d);
        double den_d = 0;
        double num_d = 0;
        double nsq = Math.sqrt((a+b)*(a+c));
        double s = 0.0;

        switch (DistanceType) {
            case 1:                                    //simple matching
                s = (a + d)/p;
                break;
            case 2:                               //Rogers-Tanimoto
                s = (a + d)/(p + b + c);
                break;
            case 3:                               //Jaccard/Tanimoto
                if(d < p) { s = a/(a+b+c); }
                else{ s = 0; }
                break;
            case 4:                                //Gleason/Dice/Sorensen/Nei-Li
                if (d < p) { s = (2*a)/(2*a + b + c); }
                else { s = 0; }
                break;
            case 5:                                //Russel-Rao
                s = a/p;
                break;
            case 6:                                //Forbes (rescaled) (Mozley)
                if (d == p){ s = 0; }
                else if (a > 0) { s = ((p*a)/((a+b)*(a+c)))/(p/a); }
                else { s = 0; }
                break;
            case 7:                               //Simpson
                double nmin = Math.min((a+b),(a+c));
                if (d < p && nmin > 0 && a > 0) { s = a/nmin; }
                else { s = 0; }
                break;
            case 8:                               //Braun-Blanquet
                if (d < p) { s = a/Math.max((a+b),(a+c)); }
                else {s = 0;}
                break;
            case 9:                               //Driver-Kroeber/Ochiai
                if (nsq > 0) { s = a/nsq; }
                else {s = 0;}
                break;
            case 10:                              //Baroni-Urbani 1
                if (d == p | a == p) { s = 1; }
                else if (nsq > 0) { s = (Math.sqrt(a*d) + a)/nsq; }
                else { s = 0; }
                break;
            case 11:                              //Kulczynski 1
                if (d == p) { s = 0; }
                else if ((a+b) > 0 && (a+c) > 0 ) { s = 0.5*(a/(a+b) + a/(a+c)); }
                else if ((a+b) == 0 ) { s = 0.5*(a/(a+c)); }
                else if ((a+c) == 0) { s = 0.5*(a/(a+b)); }
                break;
            case 12:                              //Sokal-Sneath (1)
                //System.out.println("misura 12");
                if (d < p) { s = a/(a+2*b+2*c); }
                else { s = 0; }
                break;
            case 13:                              //Sokal-Sneath (2)
                s = (2*a+2*d)/(p+a+d);
                break;
            case 14:                               //Jaccard 2
                if (d < p) { s = (3*a)/(3*a+b+c); }
                else { s = 0; }
                break;
            case 15:                               //Faith
                s = (a+0.5*d)/p;
                break;
            case 16:                              //Mountford (rescaled)
                double den = a*b+a*c+2*b*c;
                if (den > 0) { s = (2*a/den)/2; }
                else if (a == p) { s = 1; }
                else if ((b+c) == 0) { s = a/p; }
                else { s = 0; }
                break;
            case 17:                               //Michael (rescaled)
                double num = 4*(a*d-b*c);
                den = Math.pow((a+d), 2) + Math.pow((b+c), 2);
                if (d == p | a == p) { s = 1; }
                else if (den > 0 & num != 0) { s = ((num/den)+1)/2; }
                else { s = 0; }
                break;
            case 18:                               //Rogot-Goldberg
                if (d == p | a == p) { s = 1; }
                else if (d == 0 & a > 0) { s = a/((a+b)+(a+c)); }
                else if (a == 0 & d > 0) { s = d/((c+d)+(b+d)); }
                else { s = (a/((a+b)+(a+c))) + (d/((c+d)+(b+d))); }
                break;
            case 19:                               //Hawkins-Dotson
                //if d == p | a == p
                if( (b+c) == 0 ){ s = 1; }
                else if (d == 0) { s = a/(a+b+c); }
                else if (a == 0) { s = d/(b+c+d); }
                else { s = 0.5*(a/(a+b+c) + d/(b+c+d)); }
                break;
            case 20:                              //Yule 1
                num = a*d-b*c;
                den = a*d + b*c;
                if (d == p | a == p) { s = 1; }
                else if (den > 0) { s = ((num/den)+1)/2; }
                else { s = 0; }
                break;
            case 21:                              //Yule 2
                num_d = Math.sqrt(a*d) - Math.sqrt(b*c);
                den_d = Math.sqrt(a*d) + Math.sqrt(b*c);
                if (d == p | a == p) { s = 1; }
                else if (den_d > 0) { s = ((num_d/den_d)+1)/2; }
                else { s = 0; }
                break;
            case 22:                             //Fossum
                num_d = p*(Math.pow((a-0.5), 2));
                den_d = (a+b)*(a+c);
                double maxval =  ((Math.pow((p-0.5), 2)))/(p);
                if (den_d > 0) { s = (num_d/den_d)/maxval; }
                else { s = 0; }
                break;
            case 23:                             //Dennis
                num = a*d-b*c;
                den_d = Math.sqrt(p*(a+b)*(a+c));
                double minval = p/(2*Math.sqrt(p));
                double maxvalo = (p-1)/(Math.sqrt(p));
                if (d == p | a == p) {s = 1;}
                else if (den_d > 0 & num != 0) { s = ((num/den_d)+minval)/(maxvalo+minval); }
                else { s = 0; }
                break;
            case 24:                             //Cole 1
                num = a*d-b*c;
                den = (a+b)*(b+d);
                if (d == p | a == p) { s = 1; }
                else if (den > 0 & num != 0) { s = ((num/den)+(p-1))/p; }
                else { s = 0; }
                break;
            case 25:                             //Cole 2
                num = a*d-b*c;
                den = (a+c)*(c+d);
                if (d == p | a == p) { s = 1; }
                else if (den > 0 & num != 0) { s = ((num/den)+(p-1))/p; }
                else { s = 0; }
                break;
            case 26:                             //dispersion
                num = a*d-b*c;
                den = Math.pow(p, 2);
                if (d == p | a == p) { s = 1; }
                else if (den > 0 & num != 0) { s = ((num/den)+0.25)*2; }
                else { s = 0; }
                break;
            case 27:                             //Goodman-Kruskal
                num = 2*Math.min(a,d)-b-c;
                den = 2*Math.min(a,d)+b+c;
                if (d == p | a == p) { s = 1; }
                else if (den > 0 & num != 0) { s = ((num/den)+1)/(2); }
                else { s = 0; }
                break;
            case 28:                              //Sokal-Sneath 3
                double ss3 = 0;
                if ((a+b) > 0) { ss3 = ss3 + a/(a+b); }
                if ((a+c) > 0) { ss3 = ss3 + a/(a+c); }
                if ((b+d) > 0) { ss3 = ss3 + d/(b+d); }
                if ((c+d) > 0) { ss3 = ss3 + d/(c+d); }
                if (d == p | a == p) { s = 1; }
                else { s = 0.25*ss3; }
                break;
            case 29:                             //Sokal-Sneath 4
                double den1 = Math.sqrt((a+b)*(a+c));
                double den2 = Math.sqrt((b+d)*(c+d));
                if (d == p | a == p) { s = 1; }
                else if (den1 >0 & den2 > 0) { s = (a*d)/(den1*den2); }
                else { s = 0; }
                break;
            case 30:                             //Phi
                if (d == p | a == p) { s = 1; }
                else if (Q > 0) { s = (((a*d-b*c)/Math.sqrt(Q))+1)/2; }
                else { s = 0; }
                break;
            case 31:                            //Dice 1
                den = (a+b);
                if (den > 0 & d < p) { s = a/den; }
                else { s = 0; }
                break;
            case 32:                            //Dice 2
                den = (a+c);
                if (den > 0 & d < p) { s = a/den; }
                else { s = 0; }
                break;
            case 33:                            //Sorgenfrei
                den = Math.pow((a+b), 2);
                if (den > 0 & d < p) { s = (Math.pow(a, 2))/den; }
                else { s = 0; }
                break;
            case 34:                            //Cohen
                num = 2*(a*d-b*c);
                den = (a+b)*(b+d)+(a+c)*(c+d);
                if (d == p | a == p) { s = 1; }
                else if (den > 0 & num != 0) { s = ((num/den)+1)/2; }
                else { s = 0; }
                break;
            case 35:                            //Peirce 1
                num = a*d-b*c;
                den = (a+b)*(c+d);
                if (d == p | a == p) { s = 1; }
                else if (den > 0 & num != 0) { s = ((num/den)+1)/2; }
                else { s = 0; }
                break;
            case 36:                            //Peirce 2
                num = a*d-b*c;
                den = (a+c)*(b+d);
                if (d == p | a == p) { s = 1; }
                else if (den > 0 & num != 0) { s = ((num/den)+1)/2; }
                else { s = 0; }
                break;
            case 37:                            //Maxwell-Pilliner
                num = 2*(a*d-b*c);
                den = (a+b)*(c+d)+(a+c)*(b+d);
                if (d == p | a == p) { s = 1; }
                else if (den > 0 & num != 0) { s = ((num/den)+1)/2; }
                else { s = 0; }
                break;
            case 38:                           //Harris-Lahey
                double num1 = a*((c+d)+(b+d));
                double num2 = d*((a+b)+(a+c));
                den1 = 2*(a+b+c);
                den2 = 2*(b+c+d);
                if (a == p | d == p) { s = 1; }
                else if (den2 == 0) { s = (num1/den1); }
                else if (den1 == 0) { s = (num2/den2); }
                else if (den1 > 0 & den2 > 0) { s = ((num1/den1)+(num2/den2))/p; }
                else { s = 0; }
                break;
            case 39:                           //CT1
                num_d = Math.log(1+a+d);
                den_d = Math.log(1+p);
                s = num_d/den_d;
                break;
            case 40:                           //CT2
                num_d = Math.log(1+p) - Math.log(1+b+c);
                den_d = Math.log(1+p);
                s = num_d/den_d;
                break;
            case 41:                           //CT3
                num_d = Math.log(1+a);
                den_d = Math.log(1+p);
                s = num_d/den_d;
                break;
            case 42:                          //CT4
                if (d < p) {
                    num_d = Math.log(1+a);
                    den_d = Math.log(1+a+b+c);
                    s = num_d/den_d;
                }
                else { s = 0; }
                break;
            case 43:                           //CT5
                maxval = Math.log(1+(Math.pow(p, 2))/4);
                num_d = Math.log(1+a*d) - Math.log(1+b*c);
                den_d = maxval;
                s = ((num_d/den_d)+1)/2;
                break;
            case 44:                           //Austin-Colwell angular coeff.
                double w = Math.pow(((a+d)/p), 0.5);
                s = (2/Math.PI)*Math.asin(w);
                break;

            default:                           //simple matching
                s = (a + d)/p;
        }

        return s;
    }

    public static boolean CheckIsomorphismOnDisconnected(InsilicoMolecule A, InsilicoMolecule B) {

        if ((A==null)||(B==null))
            return false;

        // build sub-structures
        String[] SmiBufferA = A.GetSMILES().split("\\.");
        String[] SmiBufferB = B.GetSMILES().split("\\.");

        InsilicoMolecule[] StructuresA = new InsilicoMolecule[SmiBufferA.length];
        InsilicoMolecule[] StructuresB = new InsilicoMolecule[SmiBufferB.length];
        for (int i=0; i<SmiBufferA.length; i++)
            StructuresA[i] = SmilesMolecule.Convert(SmiBufferA[i]);
        for (int i=0; i<SmiBufferB.length; i++)
            StructuresB[i] = SmilesMolecule.Convert(SmiBufferB[i]);

        // check all sim between substructure;
        for (InsilicoMolecule a : StructuresA) {
            boolean match = false;
            for (int i=0; i<StructuresB.length; i++) {
                if (StructuresB[i] == null)
                    continue;
                boolean iso;
                try {
                    UniversalIsomorphismTester isomorphismTester = new UniversalIsomorphismTester();
                    iso = isomorphismTester.isIsomorph(a.GetStructure(), StructuresB[i].GetStructure());
                } catch (InvalidMoleculeException | CDKException ex) {
                    return false;
                }
                if (iso) {
                    StructuresB[i] = null;
                    match = true;
                    break;
                }
            }
            if (!match)
                return false;
        }
        for (InsilicoMolecule b : StructuresB) {
            if (b != null)
                return false;
        }
        return true;
    }







}
