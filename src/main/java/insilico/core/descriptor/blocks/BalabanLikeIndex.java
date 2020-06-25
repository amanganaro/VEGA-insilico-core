package insilico.core.descriptor.blocks;
import insilico.core.descriptor.Descriptor;
import insilico.core.molecule.InsilicoMolecule;


/**
 * J_Dz(v)
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class BalabanLikeIndex {

    public static double Calculate(InsilicoMolecule m) {

        try {
            double[][][] m_barysz = m.GetMatrixBarysz();
            int[][] m_adj = m.GetMatrixAdjacency();
            int nSK = m.GetStructure().getAtomCount();
            int nBO = m.GetStructure().getBondCount();
            int nCIC = m.GetSSSR().getAtomContainerCount();

            double[] VS = new double[nSK]; // barysz matrix row sums
            for (int i=0; i<(nSK); i++) {
                VS[i] = 0;
                for (int j=0; j<(nSK); j++)
                    VS[i] += m_barysz[i][j][0];
            }

            double J_Dz_v = 0;
            for (int i=0; i<(nSK-1); i++)
                for (int j=(i+1); j<nSK; j++) {
                    if (i!=j)
                        if (m_adj[i][j] != 0)
                            J_Dz_v += Math.pow(VS[i]*VS[j], -0.5);
                }

            J_Dz_v *= ((double)nBO)/((double)nCIC+1);
            return J_Dz_v;

        } catch (Exception ex) {
            return Descriptor.MISSING_VALUE;
        }

    }
}
