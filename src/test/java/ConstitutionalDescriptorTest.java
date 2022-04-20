import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.blocks.Constitutional;
import insilico.core.exception.DescriptorNotFoundException;
import insilico.core.molecule.conversion.SmilesMolecule;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class ConstitutionalDescriptorTest {


    DescriptorBlock constitutional;

    public ConstitutionalDescriptorTest(){
        constitutional = new Constitutional();
        constitutional.Calculate(SmilesMolecule.Convert("OCc1cc2ccc3cccc4ccc(c1)c2c34"));
    }

    @Test
    public void testConstitutional() throws DescriptorNotFoundException {
        Assertions.assertEquals(19.341382181515407, constitutional.GetByName("MW").getValue());
        Assertions.assertEquals(0.6447127393838469, constitutional.GetByName("AMW").getValue());
        Assertions.assertEquals(20.875121477162295, constitutional.GetByName("Sv").getValue());
        Assertions.assertEquals(0.6958373825720765, constitutional.GetByName("Mv").getValue());
        Assertions.assertEquals(22.02272727272727, constitutional.GetByName("Sp").getValue());
        Assertions.assertEquals(0.734090909090909, constitutional.GetByName("Mp").getValue());
        Assertions.assertEquals(29.629090909090905, constitutional.GetByName("Se").getValue());
        Assertions.assertEquals(0.9876363636363635, constitutional.GetByName("Me").getValue());
        Assertions.assertEquals(32.701082564407706, constitutional.GetByName("Si").getValue());
        Assertions.assertEquals(1.0900360854802569, constitutional.GetByName("Mi").getValue());
        Assertions.assertEquals(30.0, constitutional.GetByName("nAt").getValue());
        Assertions.assertEquals(18.0, constitutional.GetByName("nSk").getValue());
        Assertions.assertEquals(33.0, constitutional.GetByName("nBt").getValue());
        Assertions.assertEquals(21.0, constitutional.GetByName("nBo").getValue());

        Assertions.assertEquals(19.0, constitutional.GetByName("nBm").getValue());
        Assertions.assertEquals(0.0, constitutional.GetByName("nDB").getValue());
        Assertions.assertEquals(0.0, constitutional.GetByName("nTB").getValue());
        Assertions.assertEquals(19.0, constitutional.GetByName("nAB").getValue());
        Assertions.assertEquals(30.5, constitutional.GetByName("SCBO").getValue());

    }
}
