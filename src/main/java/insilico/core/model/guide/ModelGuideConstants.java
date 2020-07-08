package insilico.core.model.guide;

/**
 * Text (constants) for the model guide generation.
 * 
 * @author Alberto
 */
public class ModelGuideConstants {
  
    public final static String NULL_STRING = "No information available.";
        
    public final static String AD_INTRO = "The applicability domain of predictions is assessed using an Applicability Domain Index (ADI) that has values from 0 (worst case) to 1 (best case). The ADI is calculated by grouping several other indices, each one taking into account a particular issue of the applicability domain. Most of the indices are based on the calculation of the most similar compounds found in the training and test set of the model, calculated by a similarity index that consider molecule's fingerprint and structural aspects (count of atoms, rings and relevant fragments). Note that when the experimental value for the given compound is found, the Applicability Domain indices are calculated only considering this value, without taking into account the first n similar compounds.\n" +
        "\n" +
        "For each index, including the final ADI, three intervals for its values are defined, such that the first interval corresponds to a positive evaluation, the second one corresponds to a suspicious evaluation  and the last one corresponds to a negative evaluation.\n" +
        "\n" +
        "Following, all applicability domain components are reported along with their explanation and the intervals used.";
    
    public final static String AD_INTERVALS = " Defined intervals are:";
    
    public final static String STATISTICS_INTRO = "Following, statistics obtained applying the model to its original dataset:";
    
    public final static String INPUT_GENERAL = "The model accepts as input two molecule formats: SDF (multiple MOL file) and SMILES. All molecules found as input are preprocessed before the calculation of molecular descriptors, in order to obtain a standardized representation of compound. For this reason, some cautions should be taken.\n" +
        "\n" +
        "- Hydrogen atoms. In SDF files, hydrogen atoms should be explicit. As some times SDF file store only skeleton atoms, and hydrogen atoms are implicit, during the processing of the molecule the system tries to add implicit hydrogens on the basis of the known standard valence of each atom (for example, if a carbon atoms has three single bonds, an hydrogen atom will be added such to reach a valence of four). In SMILES molecules, the default notation uses implicit hydrogen. Anyway please note that in some cases it is necessary to explicitly report an hydrogen; this happens when the conformation is not unambiguous. For example, when a nitrogen atom is into an aromatic ring with a notation like \"cnc\" it is not clear whether it corresponds to C-N=C or to C-[NH]-C, thus if the situation is the latter, it should be explicitly reported as \"c[nH]c\".\n" +
        "\n" +
        "- Aromaticity. The system calculates aromaticity using the basic Hueckel rule. Note that each software for drawing and storing of molecules can use different approaches to aromaticity (for instance, commonly the user can choose between the basic Hueckel rule and a loose approach that lead to considering aromatic a greater number of rings). As in the input files aromaticity can be set explicitly (for instance, in SMILES format by using lowercase letters), during the processing of the molecule the system removes aromaticity from rings that don't satisfy the Hueckel rule. Please note that when aromaticity is removed from a ring, it is not always possible to rebuild the original structure in Kekule form (i.e. with an alternation of single and double bonds, like in the SMILES for benzene, C=1C=CC=CC1), in this case all bonds are set to single. Furthermore, please note that aromaticity detection is a really relevant issue, some molecular descriptors can have significantly different values whether a ring is perceived as aromatic or not. For this reason it is strongly recommended:\n" +
        "- Always use explicit hydrogens in SDF file.\n" +
        "- Avoid explicit aromaticity notation in original files; in this way, the perception of aromaticity is left to the preprocessing step and there is no chance of mistakes due to the transformation of rings that were set to aromatic in the original format but not recognized as aromatic in VEGA.\n" +
        "\n" +
        "Note that when some modification of the molecule are performed during the preprocessing (e.g. adding of lacking hydrogens, correction of aromaticity), a warning is given in the remark field of the results.";
    
    public final static String OUTPUT_GENERAL = "Results given as text file consist of a plain-text tabbed file (easily importable and processable by any spreadsheet software) containing in each row all the information about the prediction of a molecule. Note that if some problems were encountered while processing the molecule structure, some warning are reported in the last field (Remarks).\n" +
        "\n" +
        "Results given as PDF file consists of a document containing all the information about the prediction. For each molecule, results are organized in sections with the following order:";    
    
    public final static String OUTPUT_3_1 = "Here it is reported the list of the six most similar compounds found in the training and test set of the model, along with their depiction and relevant information (mainly experimental value and predicted value).";
    
    public final static String OUTPUT_3_2 = "Here it is reported the list of all Applicability Domain scores, starting with the global Applicability Domain Index (ADI). Note that the final assessment on prediction reliability is given on the basis of the value of the ADI. For each index, it is reported its value and a brief explanation of the meaning of that value.";
    
    public final static String OUTPUT_4_1 = "If some rare and/or missing Atom Centered Fragments are found, they are reported here with a depiction of each fragment.\n" +
        "If some relevant fragments are found, they are reported here (one for each page) with a brief explanation of their meaning and the list of the three most similar compounds that contain the same fragment. Note that if no relevant fragments are found, this section is not shown.";
        
}
