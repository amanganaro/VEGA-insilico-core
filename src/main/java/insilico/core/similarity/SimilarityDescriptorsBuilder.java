package insilico.core.similarity;

import insilico.core.descriptor.DescriptorsEngine;
import insilico.core.descriptor.blocks.Constitutional;
import insilico.core.descriptor.blocks.FunctionalGroups;
import insilico.core.exception.DescriptorNotFoundException;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.fingerprint.*;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/**
 * Engine for the calculation of descriptors needed by similarity algorithm.
 * Calculation method has an overload to enable faster calculation when
 * some descriptor blocks have been already calculated and are available.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class SimilarityDescriptorsBuilder {

    private FunctionalGroups FGDescriptors;
    private IFingerprinter FP;
    private int FPType;

    // Fingeprints types
    public final static int FP_DEFAULT = 1;
    public final static int FP_EXTENDED = 2;
    public final static int FP_GRAPHONLY = 3;
    public final static int FP_HYBRIDIZATION = 4;
    public final static int FP_ESTATE = 5;
    public final static int FP_KLEKOTA_ROTH = 6;
    public final static int FP_MACCS = 7;
    public final static int FP_PUBCHEM = 8;
    public final static int FP_SUBSTRUCTURE = 9;

    /**
     * Constructor. Default fingerprint is set to Extended Fingerprint.
     */
    public SimilarityDescriptorsBuilder() {
        FGDescriptors = null; // Created only once, first time they are needed
        FPType = FP_EXTENDED; // default FP: extended
        this.SetFingerPrint(FPType);
    }

    /**
     * Sets the fingerprint type to be used.
     * @param FingerPrintType type of fingerprint to be selected
     */
    public final void SetFingerPrint(int FingerPrintType) {
        switch (FingerPrintType) {
            case FP_DEFAULT:
                FP = new Fingerprinter();
                break;
            case FP_EXTENDED:
                FP = new ExtendedFingerprinter();
                break;
            case FP_GRAPHONLY:
                FP = new GraphOnlyFingerprinter();
                break;
            case FP_HYBRIDIZATION:
                FP = new HybridizationFingerprinter();
                break;
            case FP_ESTATE:
                FP = new EStateFingerprinter();
                break;
            case FP_KLEKOTA_ROTH:
                FP = new KlekotaRothFingerprinter();
                break;
            case FP_MACCS:
                FP = new MACCSFingerprinter();
                break;
            case FP_PUBCHEM:
                // TODO: 15/06/2020 Builder corretto?
                FP = new PubchemFingerprinter(SilentChemObjectBuilder.getInstance());
                break;
            case FP_SUBSTRUCTURE:
                FP = new SubstructureFingerprinter();
                break;
            default:
                FP = null;
                break;
        }
    }

    public SimilarityDescriptors Calculate(InsilicoMolecule Mol) {
        return this.Calculate(Mol, null);
    }

    /**
     * Returns the type of fingeprint actually selected.
     * @return the type of fingerprint
     */
    public int GetFingerPrintType() {
        return this.FPType;
    }

    /**
     * Calculates descriptors to be used for similarity and returns them
     * in a SimilarityDescriptors wrapper object.<p>
     * If Constitutional and/or Functional Groups have already been calculated
     * for the given molecule, they can be passed as argument and they will
     * not be calculated again; otherwise, if null is given as parameter, the
     * descriptor block will be calculated inside this method.
     *
     * @param Mol molecule to be processed

     * @return the descriptors used for similarity
     */
    public SimilarityDescriptors Calculate(InsilicoMolecule Mol, DescriptorsEngine DescEngine) {

        SimilarityDescriptors DescObj = new SimilarityDescriptors();

        // Calculate constitutional (or uses the cached objects)
        Constitutional curConst;
        if ( (DescEngine == null) || (!DescEngine.hasDescriptorBlock(Constitutional.class)) ){
            curConst = new Constitutional();
            curConst.Calculate(Mol);
            DescObj.Constitutional = curConst.GetAllValues();
        } else {
            curConst = (Constitutional)DescEngine.GetDescriptorBlock(Constitutional.class);
        }
        DescObj.Constitutional = curConst.GetAllValues();

        // Calculate functional groups (or uses the objects passed as parameters)
        if ( (DescEngine == null) || (!DescEngine.hasDescriptorBlock(FunctionalGroups.class)) ){
            if (FGDescriptors == null)
                FGDescriptors = new FunctionalGroups();
            FGDescriptors.Calculate(Mol);
            DescObj.FunctionalGroups = FGDescriptors.GetAllValues();
        } else {
            DescObj.FunctionalGroups = DescEngine.GetDescriptorBlock(FunctionalGroups.class).GetAllValues();
        }

        // Builds HeteroAtoms group
        DescObj.HeteroAtoms = new double[11];
        try {
            DescObj.HeteroAtoms[0] = curConst.GetByName("nN").getValue();
            DescObj.HeteroAtoms[1] = curConst.GetByName("nO").getValue();
            DescObj.HeteroAtoms[2] = curConst.GetByName("nP").getValue();
            DescObj.HeteroAtoms[3] = curConst.GetByName("nS").getValue();
            DescObj.HeteroAtoms[4] = curConst.GetByName("nF").getValue();
            DescObj.HeteroAtoms[5] = curConst.GetByName("nCl").getValue();
            DescObj.HeteroAtoms[6] = curConst.GetByName("nBr").getValue();
            DescObj.HeteroAtoms[7] = curConst.GetByName("nI").getValue();
            DescObj.HeteroAtoms[8] = curConst.GetByName("nB").getValue();
            DescObj.HeteroAtoms[9] = curConst.GetByName("nHet").getValue();
            DescObj.HeteroAtoms[10] = curConst.GetByName("nX").getValue();
        } catch (DescriptorNotFoundException e) {
            DescObj.HeteroAtoms = null;
        }

        // Calculate fp
        try {
            DescObj.Fingerprint = FP.getFingerprint(Mol.GetStructure());
        } catch (Throwable ex) {
            DescObj.Fingerprint = null;
        }

        return DescObj;
    }

}
