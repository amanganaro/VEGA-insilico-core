package insilico.core.model.qmrf;

import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import org.w3c.dom.*;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.net.URL;
import java.util.ArrayList;

import static com.lowagie.text.Element.*;
import static com.lowagie.text.Rectangle.BOX;

/**
 * Wrapper for the QMRF document read from an XML file, based on the official format (http://qmrf.sourceforge.net/qmrf.dtd)
 * Loaded document can be exported as a PDF
 */
public class QMRFDocument {

    // Tags for the main section of the QMRF document
    private final static String[] QMRF_SECTIONS = {
            "QSAR_identifier",
            "QSAR_General_information",
            "QSAR_Endpoint",
            "QSAR_Algorithm",
            "QSAR_Applicability_domain",
            "QSAR_Robustness",
            "QSAR_Predictivity",
            "QSAR_Interpretation",
            "QSAR_Miscelaneous",
            "QMRF_Summary",
    };

    public class QMRFChapter {
        public String Tag;
        public String Number;
        public String Name;
        public String Text;
    }

    public class QMRFSection {
        public String Tag;
        public String Number;
        public String Name;
        public ArrayList<QMRFChapter> Chapters;
        public QMRFSection(String Tag, String Number, String Name) {
            this.Tag = Tag;
            this.Number = Number;
            this.Name = Name;
            this.Chapters = new ArrayList<>();
        }
    }

    private final Document doc;
    private final ArrayList<QMRFSection> Sections;


    public QMRFDocument(URL XMLSource) throws InitFailureException, GenericFailureException {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            // Skip DTD validation
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(true);
            dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            dbFactory.setFeature("http://xml.org/sax/features/validation", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new DataInputStream(XMLSource.openStream()));
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            throw new InitFailureException("Unable to open document - " + e.getMessage());
        }

        Sections = new ArrayList<>();

        for (String CurSectionName : QMRF_SECTIONS) {
            Element SectionRoot = (Element) doc.getElementsByTagName(CurSectionName).item(0);
            if (SectionRoot == null)
                throw new GenericFailureException("Unable to locate tag: " + CurSectionName);
            QMRFSection curSection = new QMRFSection(SectionRoot.getTagName(), ((Element) SectionRoot).getAttribute("chapter"), ((Element) SectionRoot).getAttribute("name"));
            NodeList children = SectionRoot.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node n = children.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE)
                    curSection.Chapters.add(ExtractChapter(n));
            }
            Sections.add(curSection);
        }

    }


    private QMRFChapter ExtractChapter(Node n) {
        QMRFChapter chapter = new QMRFChapter();
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            chapter.Tag = n.getNodeName();
            NamedNodeMap attr = n.getAttributes();
            Node buf = attr.getNamedItem("chapter");
            if (buf!=null)
                chapter.Number = buf.getNodeValue();
            buf = attr.getNamedItem("name");
            if (buf!=null)
                chapter.Name = buf.getNodeValue();
            chapter.Text = n.getTextContent().trim();
        }
        return chapter;
    }


    public void PrintToScreen(boolean Values, boolean Tags) {
        for (QMRFSection s : Sections) {
            System.out.println(s.Number + " " + s.Name + (Tags?(" <" + s.Tag + ">"):""));
            for (QMRFChapter c : s.Chapters) {
                System.out.println("  " + c.Number + " " + c.Name + (Tags?(" <" + c.Tag + ">"):""));
                if (Values)
                    System.out.println("    " + (((c.Text == null)||(c.Text.isEmpty()))?"[no value]":c.Text) );
            }
        }
    }

//    public void PrintInternal() {
//        String sections = "";
//        String chapters = "";
//
//        for (QMRFSection s : Sections) {
//
//            sections += "{\"" + s.Number + "\", \"" + s.Name + "\", \"" + s.Tag + "\"},\n";
//            for (QMRFChapter c : s.Chapters)
//                chapters += "{\"" + c.Number + "\", \"" + c.Name + "\", \"" + c.Tag + "\", \"" + s.Number + "\"},\n";
//        }
//
//        System.out.println(sections);
//        System.out.println();
//        System.out.println(chapters);
//    }

    public byte[] CreatePDF() throws Exception {

        // create pdf doc and init
        com.lowagie.text.Document document = new com.lowagie.text.Document(PageSize.A4);
        ByteArrayOutputStream doc_bos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, doc_bos);
        document.open();

        // create fonts
        Font font = new Font(Font.HELVETICA, 9, Font.NORMAL);
        Font fontForSpacing = new Font(Font.HELVETICA, 3, Font.NORMAL);
        Font fontForSpacingBig = new Font(Font.HELVETICA, 6, Font.NORMAL);
        Font fontTitle = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font fontTitleBig = new Font(Font.HELVETICA, 11, Font.NORMAL);
        Font fontTitleBigBold = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font fontReference = new Font(Font.HELVETICA, 9, Font.BOLDITALIC);
        Font fontLink = new Font(Font.HELVETICA, 9, Font.BOLD + Font.UNDERLINE);
        fontLink.setColor(Color.BLUE);

        PdfPTable table;
        PdfPCell cell;

        // headers
        table = new PdfPTable(2);
        table.getDefaultCell().setBorder(BOX);

        URL uImage = getClass().getResource("/images/qmrf.png" );
        com.lowagie.text.Image img_logo = com.lowagie.text.Image.getInstance(ImageIO.read(uImage.openStream()),null);

        cell = new PdfPCell(img_logo, true);
        cell.setPadding(2);
        cell.setHorizontalAlignment(ALIGN_LEFT);
        cell.setVerticalAlignment(ALIGN_MIDDLE);
        cell.setBorderColor(new Color(20,20,20));
        cell.setBackgroundColor(Color.WHITE);
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setPadding(5);
        cell.setHorizontalAlignment(ALIGN_LEFT);
        cell.setVerticalAlignment(ALIGN_TOP);
        cell.setBorderColor(new Color(20,20,20));
        cell.setBackgroundColor(Color.WHITE);
        Paragraph par = new Paragraph("QMRF for model:", fontTitleBig);
        cell.addElement(par);
        par = new Paragraph("\n", fontForSpacing);
        cell.addElement(par);
        String st = Sections.get(0).Chapters.get(0).Text;
        par = new Paragraph(Sections.get(0).Chapters.get(0).Text, fontTitleBigBold);
        cell.addElement(par);
        table.addCell(cell);

        table.setWidthPercentage(100);
        float[] widthsHeaders = {15f, 85f};
        table.setWidths(widthsHeaders);
        document.add(table);
        document.add(new Paragraph("\n", font));

        // cycle on sections and chapters
        for (QMRFSection s : Sections) {
            String SectionTitle = s.Number + "." + s.Name;

            document.add(new Paragraph("\n", fontForSpacingBig));
            table = new PdfPTable(1);
            table.getDefaultCell().setBorder(BOX);
            cell = new PdfPCell(new Paragraph(SectionTitle, fontTitle));
            cell.setPadding(5);
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(new Color(20,20,20));
            cell.setBackgroundColor(new Color(200,200,200));
            table.addCell(cell);
            table.setWidthPercentage(100);
            float[] widths = {100f};
            table.setWidths(widths);
            document.add(table);
            document.add(new Paragraph("\n", fontForSpacingBig));

            for (QMRFChapter c : s.Chapters) {
                String ChapterTitle = c.Number + "." + c.Name;
                String[] Value = c.Text.split("\n");

                Paragraph sectionTitle = new Paragraph(ChapterTitle, fontTitle);
                document.add(sectionTitle);

                for (String line : Value) {
                    Paragraph sectionContent = new Paragraph(line, font);
                    sectionContent.setIndentationLeft(10);
                    document.add(sectionContent);
                    document.add(new Paragraph("\n", fontForSpacing));
                }
            }
        }

        document.close();
        return doc_bos.toByteArray();

    }

}
