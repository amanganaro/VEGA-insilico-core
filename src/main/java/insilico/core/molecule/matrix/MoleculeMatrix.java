package insilico.core.molecule.matrix;

import insilico.core.exception.MatrixNotSupportedException;

/**
 * Container for a generic matrix with molecular data.<p>
 * Basic methods for retrieving different type of matrices are provided. They
 * throw an Exception if the matrix requested is not the one stored.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MoleculeMatrix implements Cloneable {

    private Class MatrixClass;

    private double[][] MatrixDouble2D;
    private double[][][] MatrixDouble3D;
    private int[][] MatrixInteger2D;


    private MoleculeMatrix() {
        this.MatrixClass = null;
        this.MatrixDouble2D = null;
        this.MatrixDouble3D = null;
        this.MatrixInteger2D = null;
    }


    public MoleculeMatrix(Class MatrixClass, double[][] Matrix) {
        this();
        this.MatrixClass = MatrixClass;
        MatrixDouble2D = Matrix;
    }

    public MoleculeMatrix(Class MatrixClass, double[][][] Matrix) {
        this();
        this.MatrixClass = MatrixClass;
        MatrixDouble3D = Matrix;
    }

    public MoleculeMatrix(Class MatrixClass, int[][] Matrix) {
        this();
        this.MatrixClass = MatrixClass;
        MatrixInteger2D = Matrix;
    }


    public Class getMatrixClass() {
        return this.MatrixClass;
    }

    public double[][] getBidimensionalDoubleMatrix()
            throws MatrixNotSupportedException {
        if (MatrixDouble2D == null)
            throw new MatrixNotSupportedException();
        else
            return MatrixDouble2D;
    }

    public int[][] getBidimensionalIntMatrix()
            throws MatrixNotSupportedException {
        if (MatrixInteger2D == null)
            throw new MatrixNotSupportedException();
        else
            return MatrixInteger2D;
    }

    public double[][][] getThreedimensionalDoubleMatrix()
            throws MatrixNotSupportedException {
        if (MatrixDouble3D == null)
            throw new MatrixNotSupportedException();
        else
            return MatrixDouble3D;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MoleculeMatrix newMat = (MoleculeMatrix)super.clone();
        newMat.MatrixClass = this.MatrixClass;
        return((Object)newMat);
    }
}
