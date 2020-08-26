package insilico.core.test;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;

/**
 *
 * @author Alberto
 */
public class TestDescriptors {


    public TestDescriptors() {
        
    }


    public static void Run(DescriptorBlock block, String dataset, PrintStream outFile, PrintStream out) throws Exception {

        // calculate foo molecule just to create descriptors name list
        block.Calculate(SmilesMolecule.Convert("CCC"));
        
        StringBuilder h = new StringBuilder("No.\tMols");

        for (Descriptor d : block.GetAllDescriptors())
            h.append("\t").append(d.getName());

        outFile.println(h);
        out.println(h);

        DataInputStream in;
        BufferedReader br;

        URL TsURL = TestDescriptors.class.getClassLoader().getResource(dataset + ".txt");
        in = new DataInputStream(TsURL.openStream());
        br = new BufferedReader(new InputStreamReader(in));
        
        int count = 1;
        String s = "";
        while ( (s = br.readLine())!= null ) {       
            StringBuilder o = new StringBuilder(count + "\t" + s);
            InsilicoMolecule mol = SmilesMolecule.Convert(s);            
            if (mol.IsValid()) {
                block.Calculate(mol);
                for (Descriptor d : block.GetAllDescriptors())
                    o.append("\t").append(d.getFormattedValue());
            }
            out.println(o);
            outFile.println(o);
            count++;
        }
    }
    
        
}
