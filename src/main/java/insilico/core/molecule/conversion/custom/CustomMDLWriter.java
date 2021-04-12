package insilico.core.molecule.conversion.custom;

import insilico.core.localization.StringSelectorCore;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.config.XMLIsotopeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.DefaultChemObjectWriter;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.formats.MDLFormat;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Writes MDL molfiles, which contains a single molecule.
 * For writing a MDL molfile you can this code:
 * <pre>
 * CustomMDLWriter writer = new CustomMDLWriter(new FileWriter(new File("output.mol")));
 * writer.write((Molecule)molecule);
 * writer.close();
 * </pre>
 */
@Slf4j
public class CustomMDLWriter extends DefaultChemObjectWriter {
    
    private BufferedWriter writer;

    /**
     * Constructs a new CustomMDLWriter that can write a molecule as an {@link org.openscience.cdk.interfaces.IAtomContainer}
     * to the MDL molfile format.
     *
     * @param   out  The Writer to write to
     */
    public CustomMDLWriter(Writer out) {
        if (out instanceof BufferedWriter) {
            writer = (BufferedWriter)out;
        } else {
            writer = new BufferedWriter(out);
        }
    }

    /**
     * Constructs a new CustomMDLWriter that can write an {@link IAtomContainer} molecule
     * to a given OutputStream.
     *
     * @param   output  The OutputStream to write to
     */
    public CustomMDLWriter(OutputStream output) {
        this(new OutputStreamWriter(output));
    }

    public CustomMDLWriter() {
        this(new StringWriter());
    }

//    @TestMethod("testGetFormat")
    public IResourceFormat getFormat() {
        return MDLFormat.getInstance();
    }

    public void setWriter(Writer out) throws CDKException {
        if (out instanceof BufferedWriter) {
            writer = (BufferedWriter)out;
        } else {
            writer = new BufferedWriter(out);
        }
    }

    public void setWriter(OutputStream output) throws CDKException {
        setWriter(new OutputStreamWriter(output));
    }

    /**
     * Flushes the output and closes this object.
     */
//    @TestMethod("testClose")
    public void close() throws IOException {
        writer.close();
    }

//    @TestMethod("testAccepts")
    public boolean accepts(Class classObject) {
        Class[] interfaces = classObject.getInterfaces();
        for (Class anInterface : interfaces) {
            if (IAtomContainer.class.equals(anInterface)) return true;
            if (IChemFile.class.equals(anInterface)) return true;
            if (IChemModel.class.equals(anInterface)) return true;
        }
        Class superClass = classObject.getSuperclass();
        if (superClass != null) return this.accepts(superClass);
        return false;
    }

    /**
     * Writes a {@link IChemObject} to the MDL molfile formated output.
     * It can only output ChemObjects of type {@link IChemFile}
     * and {@link IAtomContainer}.
     *
     * @param object {@link IChemObject} to write
     *
     * @see #accepts(Class)
     */
    public void write(IChemObject object) throws CDKException {
        try {
            if (object instanceof IChemFile) {
                writeChemFile((IChemFile)object);
                return;
            } else if (object instanceof IChemModel) {
                IChemFile file = object.getBuilder().newInstance(IChemFile.class);
                IChemSequence sequence = object.getBuilder().newInstance(IChemSequence.class);
                sequence.addChemModel((IChemModel)object);
                file.addChemSequence(sequence);
                writeChemFile((IChemFile)file);
                return;
            } else if (object instanceof IAtomContainer) {
                writeMolecule((IAtomContainer)object);
                return;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.debug(ex.getMessage());
            throw new CDKException(String.format(StringSelectorCore.getString("conversion_mdlwriter_cdk_exception"), ex.getMessage()), ex);
        }
        throw new CDKException(StringSelectorCore.getString("conversion_mdlwriter_cdk_exception2"));
    }

    private void writeChemFile(IChemFile file) throws Exception {
        IAtomContainer bigPile = file.getBuilder().newInstance(IAtomContainer.class);
        for (IAtomContainer container :
                ChemFileManipulator.getAllAtomContainers(file)) {
            bigPile.add(container);
        }
        writeMolecule(bigPile);
    }

    /**
     * Writes a Molecule to an OutputStream in MDL sdf format.
     *
     * @param   container  Molecule that is written to an OutputStream
     */
    public void writeMolecule(IAtomContainer container) throws Exception {
        String line = "";
        // write header block
        // lines get shortened to 80 chars, that's in the spec
        String title = container.getProperty(CDKConstants.TITLE);
        if (title == null) title = "";
        if(title.length()>80)
            title=title.substring(0,80);
        writer.write(title);
        writer.newLine();

        /* From CTX spec
         * This line has the format:
         * IIPPPPPPPPMMDDYYHHmmddSSssssssssssEEEEEEEEEEEERRRRRR
         * (FORTRAN: A2<--A8--><---A10-->A2I2<--F10.5-><---F12.5--><-I6-> )
         * User's first and last initials (l), program name (P),
         * date/time (M/D/Y,H:m), dimensional codes (d), scaling factors (S, s),
         * energy (E) if modeling program input, internal registry number (R)
         * if input through MDL form.
         * A blank line can be substituted for line 2.
         */
        writer.write("  CDK    ");
        writer.write(new SimpleDateFormat("M/d/y,H:m", Locale.US).format(
                Calendar.getInstance(TimeZone.getDefault()).getTime())
        );
        writer.newLine();

        String comment = (String)container.getProperty(CDKConstants.REMARK);
        if (comment == null) comment = "";
        if(comment.length()>80)
            comment=comment.substring(0,80);
        writer.write(comment);
        writer.newLine();

        // write Counts line
        line += formatMDLInt(container.getAtomCount(), 3);
        line += formatMDLInt(container.getBondCount(), 3);
        line += "  0  0  0  0  0  0  0  0999 V2000";
        writer.write(line);
        writer.newLine();

        // write Atom block
        for (int f = 0; f < container.getAtomCount(); f++) {
            IAtom atom = container.getAtom(f);
            line = "";
            if (atom.getPoint3d() != null) {
                line += formatMDLFloat((float) atom.getPoint3d().x);
                line += formatMDLFloat((float) atom.getPoint3d().y);
                line += formatMDLFloat((float) atom.getPoint3d().z) + " ";
            } else if (atom.getPoint2d() != null) {
                line += formatMDLFloat((float) atom.getPoint2d().x);
                line += formatMDLFloat((float) atom.getPoint2d().y);
                line += "    0.0000 ";
            } else {
                // if no coordinates available, then output a number
                // of zeros
                line += formatMDLFloat((float)0.0);
                line += formatMDLFloat((float)0.0);
                line += formatMDLFloat((float)0.0) + " ";
            }
            if(container.getAtom(f) instanceof IPseudoAtom){
                //according to http://www.google.co.uk/url?sa=t&ct=res&cd=2&url=http%3A%2F%2Fwww.mdl.com%2Fdownloads%2Fpublic%2Fctfile%2Fctfile.pdf&ei=MsJjSMbjAoyq1gbmj7zCDQ&usg=AFQjCNGaJSvH4wYy4FTXIaQ5f7hjoTdBAw&sig2=eSfruNOSsdMFdlrn7nhdAw an R group is written as R#
                if(((IPseudoAtom) container.getAtom(f)).getLabel().equals("R"))
                    line += "R#";
                else
                    line += formatMDLString(((IPseudoAtom) container.getAtom(f)).getLabel(), 3);
            }else{
                line += formatMDLString(container.getAtom(f).getSymbol(), 3);
            }
            line += " 0  0  0  0  0  0  0  0  0  0  0  0";
            writer.write(line);
            writer.newLine();
        }

        // write Bond block
        Iterator<IBond> bonds = container.bonds().iterator();
        while (bonds.hasNext()) {
            IBond bond = (IBond) bonds.next();

            if (bond.getAtomCount() != 2) {
                log.warn(StringSelectorCore.getString("conversion_mdlwriter_skip_bond") + bond);
            } else {

                // Modified by AM - no stero information taken into account

//                        if (bond.getStereo() == CDKConstants.STEREO_BOND_UP_INV ||
//        				bond.getStereo() == CDKConstants.STEREO_BOND_DOWN_INV) {
//        			// turn around atom coding to correct for inv stereo
//        			line = formatMDLInt(container.getAtomNumber(bond.getAtom(1)) + 1,3);
//        			line += formatMDLInt(container.getAtomNumber(bond.getAtom(0)) + 1,3);
//        		} else {
//        			line = formatMDLInt(container.getAtomNumber(bond.getAtom(0)) + 1,3);
//        			line += formatMDLInt(container.getAtomNumber(bond.getAtom(1)) + 1,3);
//        		}

                line = formatMDLInt(container.indexOf(bond.getAtom(0)) + 1,3);
                line += formatMDLInt(container.indexOf(bond.getAtom(1)) + 1,3);


                // Modified by AM
                if (bond.getFlag(CDKConstants.ISAROMATIC))
                    line += formatMDLInt(4,3);
                else
                    line += formatMDLInt(bond.getOrder().ordinal()+1,3);


                line += "  ";

                // Modified by AM - no stero information taken into account

//        		switch(bond.getStereo()){
//        		case CDKConstants.STEREO_BOND_UP:
//        			line += "1";
//        			break;
//        		case CDKConstants.STEREO_BOND_UP_INV:
//        			line += "1";
//        			break;
//        		case CDKConstants.STEREO_BOND_DOWN:
//        			line += "6";
//        			break;
//        		case CDKConstants.STEREO_BOND_DOWN_INV:
//        			line += "6";
//        			break;
//        		default:
//        			line += "0";
//        		}
                line += "0";

                line += "  0  0  0 ";
                writer.write(line);
                writer.newLine();
            }
        }

        // write formal atomic charges
        for (int i = 0; i < container.getAtomCount(); i++) {
            IAtom atom = container.getAtom(i);
            int charge = atom.getFormalCharge();
            if (charge != 0) {
                writer.write("M  CHG  1 ");
                writer.write(formatMDLInt(i+1,3));
                writer.write(" ");
                writer.write(formatMDLInt(charge,3));
                writer.newLine();
            }
        }

        // write formal isotope information
        for (int i = 0; i < container.getAtomCount(); i++) {
            IAtom atom = container.getAtom(i);
            if (!(atom instanceof IPseudoAtom)) {
                Integer atomicMass = atom.getMassNumber();
                if (atomicMass != null) {
                    int majorMass = XMLIsotopeFactory.getInstance(atom.getBuilder()).getMajorIsotope(atom.getSymbol()).getMassNumber();
                    if (atomicMass != majorMass) {
                        writer.write("M  ISO  1 ");
                        writer.write(formatMDLInt(i+1,3));
                        writer.write(" ");
                        writer.write(formatMDLInt(atomicMass,3));
                        writer.newLine();
                    }
                }
            }
        }

        // close molecule
        writer.write("M  END");
        writer.newLine();
        writer.flush();
    }

    /**
     * Formats a float to fit into the connectiontable and changes it
     * to a String.
     *
     * @param   fl  The float to be formated
     * @return      The String to be written into the connectiontable
     */
    private String formatMDLFloat(float fl) {
        String s = "";
        StringBuilder fs = new StringBuilder();
        int l;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setMinimumIntegerDigits(1);
        nf.setMaximumIntegerDigits(4);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
        nf.setGroupingUsed(false);
        s = nf.format(fl);
        l = 10 - s.length();
        fs.append(" ".repeat(Math.max(0, l)));
        fs.append(s);
        return fs.toString();
    }



    /**
     * Formats a String to fit into the connectiontable.
     *
     * @param   s    The String to be formated
     * @param   le   The length of the String
     * @return       The String to be written in the connectiontable
     */
    private String formatMDLString(String s, int le) {
        s = s.trim();
        if (s.length() > le)
            return s.substring(0, le);
        int l;
        l = le - s.length();
        StringBuilder sBuilder = new StringBuilder(s);
        sBuilder.append(" ".repeat(l));
        s = sBuilder.toString();
        return s;
    }

    /**
     * Formats an integer to fit into the connection table and changes it
     * to a String.
     *
     * @param   i  The int to be formated
     * @param   l  Length of the String
     * @return     The String to be written into the connectiontable
     */
    private String formatMDLInt(int i, int l) {
        String s = "";
        StringBuilder fs = new StringBuilder();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setParseIntegerOnly(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMaximumIntegerDigits(l);
        nf.setGroupingUsed(false);
        s = nf.format(i);
        l = l - s.length();
        fs.append(" ".repeat(Math.max(0, l)));
        fs.append(s);
        return fs.toString();
    }




}
