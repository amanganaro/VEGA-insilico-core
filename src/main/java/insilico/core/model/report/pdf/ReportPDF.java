package insilico.core.model.report.pdf;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import insilico.core.ad.item.ADIndex;
import insilico.core.ad.item.iADIndex;

import insilico.core.ad.reasoning.ACFAnalysis;
import insilico.core.ad.reasoning.DescriptorAnalysis;
import insilico.core.ad.reasoning.Uncertainty;
import insilico.core.ad.reasoning.UncertaintyClassBar;
import insilico.core.ad.reasoning.iReasoningItem;
import insilico.core.alerts.Alert;
import insilico.core.alerts.AlertEncoding;
import insilico.core.alerts.AlertsEngine;
import insilico.core.constant.InsilicoConstants;
import insilico.core.constant.MessagesAD;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.model.InsilicoModelConsensusOutput;
//import insilico.core.model.old.InsilicoModelInfoOLD;
import insilico.core.model.InsilicoModelInfo;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.report.pdf.classbarchart.ClassBarChart;
import insilico.core.model.report.pdf.classbarchart.ClassBarDataPoint;
import insilico.core.model.report.pdf.scatterchart.ScatterChart;
import insilico.core.model.report.pdf.scatterchart.ScatterChartDataPoint;
import insilico.core.model.report.pdf.scatterchart.ScatterChartDataSet;
import insilico.core.model.runner.InsilicoModelConsensusWrapper;
import insilico.core.model.runner.InsilicoModelWrapper;
import insilico.core.model.trainingset.TrainingSet;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import insilico.core.similarity.SimilarMolecule;
import insilico.core.tools.utils.ModelUtilities;
import insilico.core.version.InsilicoInfo;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

import static com.lowagie.text.Element.*;
import static com.lowagie.text.Rectangle.BOTTOM;
@Slf4j
public class ReportPDF {


    protected Document document;
    protected ByteArrayOutputStream doc_bos;
    protected PdfWriter writer;
    protected ArrayList<InsilicoModelWrapper> modelWrappers;
    protected ArrayList<InsilicoModelConsensusWrapper> modelConsWrappers;
    protected boolean HiResMode;
    protected int CurPage;

    // For alerts in similar mols
    private final AlertsEngine SAEngine;

    // Fonts object
    private final static int DEFAULT_FONT = Font.HELVETICA;
    private Font font;
    private Font fontGray;
    private Font fontTitle;
    private Font fontTitleGreen;
    private Font fontBig;
    private Font fontSmall;

    // Images object
    private Image Img_circle_green;
    private Image Img_circle_yellow;
    private Image Img_circle_orange;
    private Image Img_circle_red;
    private Image Img_circle_gray;
    private Image Img_stars_0;
    private Image Img_stars_1;
    private Image Img_stars_2;
    private Image Img_stars_3;
    private Image Img_assessment_good;
    private Image Img_assessment_non_optimal;
    private Image Img_assessment_bad;
    private Image Img_header;

    private Image Img_title_1;
    private Image Img_title_2;
    private Image Img_title_3_1;
    private Image Img_title_3_2;
    private Image Img_title_4_1;
    private Image Img_title_4_2;

    private Image Img_title_1_highres;
    private Image Img_title_2_highres;
    private Image Img_title_3_1_highres;
    private Image Img_title_3_2_highres;
    private Image Img_title_4_1_highres;
    private Image Img_title_4_2_highres;

    // Core version
    private InsilicoInfo coreVersion;

    // Format object
    private DecimalFormat Format_3D;


    // CONSTRUCTOR
    public ReportPDF(boolean HiRes) throws InitFailureException {
        this.HiResMode = HiRes;
        FetchImageObjects(HiRes);
        CreateFonts();

        DecimalFormatSymbols InternationalSymbols =
                new DecimalFormatSymbols();
        InternationalSymbols.setDecimalSeparator('.');
        Format_3D = new DecimalFormat("0.###", InternationalSymbols);

        coreVersion = new InsilicoInfo();

        SAEngine = new AlertsEngine();
    }


   // Init Images from files
   private void FetchImageObjects(boolean HiRes) throws InitFailureException {

       // Init images
       try {
           URL uImage = null;

           uImage = getClass().getResource("/images/light_green.png" );
           Img_circle_green = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/light_yellow.png" );
           Img_circle_yellow = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/light_orange.png" );
           Img_circle_orange = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/light_red.png" );
           Img_circle_red = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/light_gray.png" );
           Img_circle_gray = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/stars_0.png" );
           Img_stars_0 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/stars_1.png" );
           Img_stars_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/stars_2.png" );
           Img_stars_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/stars_3.png" );
           Img_stars_3 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/assessment_ok.png" );
           Img_assessment_good = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/assessment_bad.png" );
           Img_assessment_bad = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/assessment_non-optimal.png" );
           Img_assessment_non_optimal = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           uImage = getClass().getResource("/images/vega_header.png" );
           Img_header = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           if (HiRes) {

               uImage = getClass().getResource("/images/hi_report_top_section_1.png" );
               Img_title_1_highres = Image.getInstance(ImageIO.read(uImage.openStream()),null);

               uImage = getClass().getResource("/images/hi_report_top_section_2.png" );
               Img_title_2_highres = Image.getInstance(ImageIO.read(uImage.openStream()),null);

               uImage = getClass().getResource("/images/hi_report_top_section_3_1.png" );
               Img_title_3_1_highres = Image.getInstance(ImageIO.read(uImage.openStream()),null);

               uImage = getClass().getResource("/images/hi_report_top_section_3_2.png" );
               Img_title_3_2_highres = Image.getInstance(ImageIO.read(uImage.openStream()),null);

               uImage = getClass().getResource("/images/hi_report_top_section_4_1.png" );
               Img_title_4_1_highres = Image.getInstance(ImageIO.read(uImage.openStream()),null);

               uImage = getClass().getResource("/images/hi_report_top_section_4_2.png" );
               Img_title_4_2_highres = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           } else {

               uImage = getClass().getResource("/images/report_top_section_1.png" );
               Img_title_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

               uImage = getClass().getResource("/images/report_top_section_2.png" );
               Img_title_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

               uImage = getClass().getResource("/images/report_top_section_3_1.png" );
               Img_title_3_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

               uImage = getClass().getResource("/images/report_top_section_3_2.png" );
               Img_title_3_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

               uImage = getClass().getResource("/images/report_top_section_4_1.png" );
               Img_title_4_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

               uImage = getClass().getResource("/images/report_top_section_4_2.png" );
               Img_title_4_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

           }
       } catch (Exception e) {
           throw new InitFailureException(StringSelectorCore.getString("report_generator_load_image_fail"));
       }
   }

    private void CreateFonts() {
        // Creates fonts
        font = new Font(DEFAULT_FONT, 10, Font.NORMAL);
        fontGray = new Font(DEFAULT_FONT, 10, Font.NORMAL);
        fontGray.setColor(Color.GRAY);
        fontTitle = new Font(DEFAULT_FONT, 10, Font.BOLD);
        fontTitleGreen = new Font(DEFAULT_FONT, 10, Font.BOLD);
        fontTitleGreen.setColor(new Color(0,136,0));
        fontBig = new Font(DEFAULT_FONT, 12, Font.BOLD);
        fontSmall = new Font(DEFAULT_FONT, 4, Font.NORMAL);
    }


    protected void InitReport(ArrayList<InsilicoModelWrapper> ResModelWrappers,
                              ArrayList<InsilicoModelConsensusWrapper> ResModelConsWrappers)
            throws InitFailureException {

        this.modelWrappers = ResModelWrappers;
        this.modelConsWrappers = ResModelConsWrappers;

        // Creates writer and opens document
        try {
            document = new Document(PageSize.A4);
            doc_bos = new ByteArrayOutputStream();
            writer = PdfWriter.getInstance(document, doc_bos);
            document.open();
        } catch (Exception e) {
            throw new InitFailureException(String.format(StringSelectorCore.getString("guide_generator_unable_create_pdf"), e.getMessage()));
        }

        for (InsilicoModelWrapper Wrapper : modelWrappers) {
            SAEngine.AddAlertsBlock(Wrapper.getModel().GetRequiredAlertBlocks());
        }
    }


    protected void InitReport(ArrayList<InsilicoModelWrapper> ResModelWrappers)
            throws InitFailureException {
        ArrayList<InsilicoModelConsensusWrapper> ConsModels = new ArrayList<>();
        this.InitReport(ResModelWrappers, ConsModels);

    }


    protected void InitReport(InsilicoModelWrapper ResModelWrapper)
            throws InitFailureException {
        ArrayList<InsilicoModelWrapper> SingleModels = new ArrayList<>();
        ArrayList<InsilicoModelConsensusWrapper> ConsModels = new ArrayList<>();
        SingleModels.add(ResModelWrapper);
        this.InitReport(SingleModels, ConsModels);
    }


    protected void InitReport(InsilicoModelConsensusWrapper ResModelConsWrapper)
            throws InitFailureException {
        ArrayList<InsilicoModelWrapper> SingleModels = new ArrayList<InsilicoModelWrapper>();
        ArrayList<InsilicoModelConsensusWrapper> ConsModels = new ArrayList<InsilicoModelConsensusWrapper>();
        ConsModels.add(ResModelConsWrapper);
        this.InitReport(SingleModels, ConsModels);

    }

    protected byte[] GenerateReport() {

        if (document.isOpen()) {
            document.close();
        }
        return doc_bos.toByteArray();

    }

    private void WritePageHeader(InsilicoModelInfo Model, int Section) throws GenericFailureException {

        Image gif = null;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        float w = PageSize.A4.getWidth();
        float h = PageSize.A4.getHeight();
        double y_pos = 0;

        try {

            // Header
            float[] widths = {25f, 50f, 25f};
            gif = Img_header;
            table = new PdfPTable(3);
            table.getDefaultCell().setFixedHeight(20);
            table.getDefaultCell().setBorder(BOTTOM);
            cell = new PdfPCell(gif, true);
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setFixedHeight(20);
            cell.setBorder(BOTTOM);
            cell.setBorderColor(new Color(20,20,20));
            table.addCell(cell);
            paragraph = new Paragraph(Model.getName() + " " + Model.getVersion(), font);
            cell = new PdfPCell(paragraph);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setFixedHeight(20);
            cell.setBorder(BOTTOM);
            cell.setBorderColor(new Color(20,20,20));
            table.addCell(cell);
            paragraph = new Paragraph(StringSelectorCore.getString("report_generator_page") + CurPage, font);
            cell = new PdfPCell(paragraph);
            cell.setHorizontalAlignment(ALIGN_RIGHT);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setFixedHeight(20);
            cell.setBorder(BOTTOM);
            cell.setBorderColor(new Color(20,20,20));
            table.addCell(cell);
            table.setTotalWidth(w-20);
            table.setWidths(widths);
            y_pos = table.writeSelectedRows(0, -1, 10, h-12, writer.getDirectContent());

            // Section title
            if (Section != -1) {

                if(HiResMode){
                    if (Section == 1)
                        gif = Img_title_1_highres;
                    if (Section == 2)
                        gif = Img_title_2_highres;
                    if (Section == 31)
                        gif = Img_title_3_1_highres;
                    if (Section == 32)
                        gif = Img_title_3_2_highres;
                    if (Section == 41)
                        gif = Img_title_4_1_highres;
                    if (Section == 42)
                        gif = Img_title_4_2_highres;
                } else {
                    if (Section == 1)
                        gif = Img_title_1;
                    if (Section == 2)
                        gif = Img_title_2;
                    if (Section == 31)
                        gif = Img_title_3_1;
                    if (Section == 32)
                        gif = Img_title_3_2;
                    if (Section == 41)
                        gif = Img_title_4_1;
                    if (Section == 42)
                        gif = Img_title_4_2;
                }


                float[] widths_title = {100f};
                table = new PdfPTable(1);
                cell = new PdfPCell(gif, true);
                cell.setHorizontalAlignment(ALIGN_CENTER);
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setBorderColor(Color.WHITE);
                table.addCell(cell);
                table.setTotalWidth(w);
                table.setWidths(widths_title);
                y_pos = table.writeSelectedRows(0, -1, 0, h-38, writer.getDirectContent());
                document.add(new Paragraph("\n\n\n\n\n", font));
            }

        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));
        }
    }


    protected void AddCover() throws GenericFailureException {

        Image gif = null;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        float w = PageSize.A4.getWidth();
        float h = PageSize.A4.getHeight();

        try {

            double y_pos = 0;

            // Header
            URL uBanner = null;
            if (HiResMode)
                uBanner = getClass().getResource("/images/hi_vega_report_cover_top.png" );
            else
                uBanner = getClass().getResource("/images/vega_report_cover_top.png" );
            gif = Image.getInstance(ImageIO.read(uBanner.openStream()),null);
            table = new PdfPTable(1);
            cell = new PdfPCell(gif, true);
            cell.setPaddingBottom(4);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setBorderColor(new Color(255,255,255));
            table.addCell(cell);
            table.setTotalWidth(w-20);
            y_pos = table.writeSelectedRows(0, -1, 10, h-10, writer.getDirectContent());

            // Footer
            if (HiResMode)
                uBanner = getClass().getResource("/images/hi_vega_report_cover_bottom.png" );
            else
                uBanner = getClass().getResource("/images/vega_report_cover_bottom.png" );
            gif = Image.getInstance(ImageIO.read(uBanner.openStream()),null);
            table = new PdfPTable(1);
            cell = new PdfPCell(gif, true);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setBorderColor(new Color(255,255,255));
            table.addCell(cell);
            table.setTotalWidth(w-20);
            int GifHeight = Math.round((w-20) * (gif.getHeight() / gif.getWidth()));
            table.writeSelectedRows(0, -1, 10, 10 + GifHeight, writer.getDirectContent());

            // Mid contents
            long MidTable_Left = Math.round(10 + (w-20) * 0.096);
            long MidTable_Width = Math.round((w-20) * 0.858);
            table = new PdfPTable(1);
            cell = new PdfPCell();
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPadding(10);
            cell.setBackgroundColor(new Color(240,240,240));
            StringBuilder CurPar = new StringBuilder(StringSelectorCore.getString("report_generator_prediction_ad_section") + "\n\n");
            paragraph = new Paragraph(CurPar.toString(), fontTitle);
            cell.addElement(paragraph);
            CurPar = new StringBuilder();
            for (InsilicoModelConsensusWrapper modelConsWrapper : modelConsWrappers) {
                CurPar.append("  ").append(modelConsWrapper.getModel().getInfo().getName()).append(" ").append(modelConsWrapper.getModel().getInfo().getVersion()).append("\n");
            }
            for (InsilicoModelWrapper modelWrapper : modelWrappers) {
                if (!modelWrapper.isFlagForOutput())
                    continue;
                CurPar.append("  ").append(modelWrapper.getModel().getInfo().getName()).append(" ").append(modelWrapper.getModel().getInfo().getVersion()).append("\n");
            }
            CurPar.append("\n\n");
            paragraph = new Paragraph(CurPar.toString(), fontBig);
            cell.addElement(paragraph);
            CurPar = new StringBuilder(StringSelectorCore.getString("report_generator_core_version") + coreVersion.getVersion());
            paragraph = new Paragraph(CurPar.toString(), fontGray);
            cell.addElement(paragraph);
            table.addCell(cell);
            table.setTotalWidth(MidTable_Width);
            table.writeSelectedRows(0, -1, MidTable_Left, Math.round(y_pos) - 5 , writer.getDirectContent());

        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));
        }

        CurPage = 0;
    }

    protected void AddReferencePage() throws GenericFailureException {

        Image gif = null;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        String CurPar;
        float w = PageSize.A4.getWidth();
        float h = PageSize.A4.getHeight();

        int modelIdx = 0;
        int nModels = modelWrappers.size() + modelConsWrappers.size();
        for (InsilicoModelWrapper mw : modelWrappers)
            if (!mw.isFlagForOutput()) nModels--;

        // Build list of model to be shown
        String[] ModelTitle = new String[nModels];
        String[] ModelDesc = new String[nModels];
        for (InsilicoModelConsensusWrapper mw : modelConsWrappers) {
            InsilicoModelInfo curInfo = mw.getModel().getInfo();
            ModelTitle[modelIdx] = curInfo.getName() + "(" + StringSelectorCore.getString("report_generator_version") + " " + curInfo.getVersion() + ")\n";
            ModelDesc[modelIdx] = curInfo.getSummary() + "\n"; ;
            modelIdx++;
        }
        for (InsilicoModelWrapper mw : modelWrappers) {
            if (!mw.isFlagForOutput())
                continue;
            InsilicoModelInfo curInfo = mw.getModel().getInfo();
            ModelTitle[modelIdx] = curInfo.getName() + "(" + StringSelectorCore.getString("report_generator_version") + " " + curInfo.getVersion() + ")\n";
            ModelDesc[modelIdx] = curInfo.getSummary() + "\n"; ;
            modelIdx++;
        }

        modelIdx = 0;

        try {

            while (modelIdx < nModels) {

                document.newPage();

                double y_pos = 0;

                // Header
                URL uBanner = null;
                if (HiResMode)
                    uBanner = getClass().getResource("/images/hi_vega_report_references_top.png" );
                else
                    uBanner = getClass().getResource("/images/vega_report_references_top.png" );
                gif = Image.getInstance(ImageIO.read(uBanner.openStream()),null);
                table = new PdfPTable(1);
                cell = new PdfPCell(gif, true);
                cell.setPaddingBottom(4);
                cell.setHorizontalAlignment(ALIGN_CENTER);
                cell.setBorderColor(new Color(255,255,255));
                table.addCell(cell);
                table.setTotalWidth(w-20);
                y_pos = table.writeSelectedRows(0, -1, 10, h-10, writer.getDirectContent());

                // Footer
                if (HiResMode)
                    uBanner = getClass().getResource("/images/hi_vega_report_references_bottom.png" );
                else
                    uBanner = getClass().getResource("/images/vega_report_references_bottom.png" );
                gif = Image.getInstance(ImageIO.read(uBanner.openStream()),null);
                table = new PdfPTable(1);
                cell = new PdfPCell(gif, true);
                cell.setHorizontalAlignment(ALIGN_CENTER);
                cell.setBorderColor(new Color(255,255,255));
                table.addCell(cell);
                table.setTotalWidth(w-20);
                int GifHeight = Math.round((w-20) * (gif.getHeight() / gif.getWidth()));
                table.writeSelectedRows(0, -1, 10, 10 + GifHeight, writer.getDirectContent());

                // Mid contents
                long MidTable_Left = Math.round(10 + (w-20) * 0.096);
                long MidTable_Width = Math.round((w-20) * 0.858);
                table = new PdfPTable(1);
                cell = new PdfPCell();
                cell.setHorizontalAlignment(ALIGN_LEFT);
                cell.setBorderColor(new Color(255,255,255));
                cell.setPadding(10);
                cell.setBackgroundColor(new Color(240,240,240));

                // general disclaimer
                if (modelIdx == 0) {
                    CurPar = StringSelectorCore.getString("report_generator_paragraph_disclaimer");
                    paragraph = new Paragraph(CurPar, fontTitle);
                    cell.addElement(paragraph);
                    cell.addElement(new Paragraph("\n", font));
                }

                // just 4 descriptions for page
                for (int i=0; i<4; i++) {
                    if (!(modelIdx < nModels))
                        break;

                    CurPar = ModelTitle[modelIdx];
                    paragraph = new Paragraph(CurPar, fontBig);
                    cell.addElement(paragraph);
                    cell.addElement(new Paragraph("\n", fontSmall));
                    CurPar = ModelDesc[modelIdx];
                    paragraph = new Paragraph(CurPar, font);
                    cell.addElement(paragraph);
                    cell.addElement(new Paragraph("\n", font));

                    modelIdx++;
                }

                table.addCell(cell);
                table.setTotalWidth(MidTable_Width);
                table.writeSelectedRows(0, -1, MidTable_Left, Math.round(y_pos) - 5 , writer.getDirectContent());

            }

        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));
        }

    }


    protected void WritePageResults(InsilicoModelWrapper ModelWrapper, InsilicoMolecule inMol, int molIdx)
            throws GenericFailureException {

        Image gif = null;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        float[] widths = {22f, 78f};


        try {

            document.newPage();
            CurPage++;
            WritePageHeader(ModelWrapper.getModel().getInfo(), 1);


            //// PAGE 1 - results //////////////////////////////////////////

            InsilicoModelOutput curOut = ModelWrapper.getResult().get(molIdx);
            String sCAS = "";
            if (!inMol.GetCAS().isEmpty())
                if (!inMol.GetCAS().equals(inMol.GetId()))
                    sCAS =  "(" + inMol.GetCAS() + ")";
//            document.add(new Paragraph("Prediction for compound " + inMol.GetId() + sCAS +"\n\n", fontTitle));
            document.add(new Paragraph(String.format(StringSelectorCore.getString("report_prediction_for_compound"), inMol.GetId(), sCAS) +  "\n\n", fontTitle));

            table = new PdfPTable(2);


            try {
                gif = Image.getInstance(Depiction.DepictMolecule(inMol, 240, 240),null);
            } catch (Exception e) {
                BufferedImage I = new BufferedImage(240, 240, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = I.createGraphics();
                graphics.setBackground(new Color(255,255,255));
                graphics.setColor(new Color(0,0,0));
                graphics.clearRect(0, 0, 240, 240);
                graphics.drawLine(20, 20, 220, 220);
                graphics.drawLine(20, 220, 220, 20);
                graphics.dispose();
                gif = Image.getInstance(I,null);
            }

            cell = new PdfPCell(gif, true);
            cell.setPadding(2);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(new Color(200,200,200));
            table.addCell(cell);

            PdfPTable tableImg = null;
            String CurPar = "";

            if (curOut.HasExperimental()) {

                tableImg = new PdfPTable(2);

                switch ( curOut.getAssessmentStatus()) {
                    case InsilicoModelOutput.ASSESS_GREEN:
                        gif = Img_circle_green;
                        break;
                    case InsilicoModelOutput.ASSESS_RED:
                        gif = Img_circle_red;
                        break;
                    case InsilicoModelOutput.ASSESS_YELLOW:
                        gif = Img_circle_yellow;
                        break;
                    case InsilicoModelOutput.ASSESS_ORANGE:
                        gif = Img_circle_orange;
                        break;
                    case InsilicoModelOutput.ASSESS_GRAY:
                        gif = Img_circle_gray;
                        break;
                }

                cell = new PdfPCell(gif, true);
                cell.setFixedHeight(20);
                cell.setHorizontalAlignment(ALIGN_LEFT);
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setBorderColor(new Color(255,255,255));
                cell.setPadding(2);
                tableImg.addCell(cell);

                cell = new PdfPCell();
                cell.setFixedHeight(20);
                cell.setPadding(2);
                cell.setHorizontalAlignment(ALIGN_LEFT);
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setBorderColor(new Color(255,255,255));
                CurPar = "EXPERIMENTAL DATA";
                paragraph = new Paragraph(CurPar, fontTitleGreen);
                cell.addElement(paragraph);
                tableImg.addCell(cell);

                tableImg.setWidthPercentage(100);
                tableImg.setHorizontalAlignment(ALIGN_LEFT);
                float[] widthsTbl = {8f, 92f};
                tableImg.setWidths(widthsTbl);

            } else {

                tableImg = new PdfPTable(4);

                cell = new PdfPCell();
                cell.setFixedHeight(20);
                cell.setPadding(2);
                cell.setHorizontalAlignment(ALIGN_LEFT);
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setBorderColor(new Color(255,255,255));
                CurPar = "Prediction: ";
                paragraph = new Paragraph(CurPar, font);
                cell.addElement(paragraph);
                tableImg.addCell(cell);

                switch ( curOut.getAssessmentStatus()) {
                    case InsilicoModelOutput.ASSESS_GREEN:
                        gif = Img_circle_green;
                        break;
                    case InsilicoModelOutput.ASSESS_RED:
                        gif = Img_circle_red;
                        break;
                    case InsilicoModelOutput.ASSESS_YELLOW:
                        gif = Img_circle_yellow;
                        break;
                    case InsilicoModelOutput.ASSESS_ORANGE:
                        gif = Img_circle_orange;
                        break;
                    case InsilicoModelOutput.ASSESS_GRAY:
                        gif = Img_circle_gray;
                        break;
                }

                cell = new PdfPCell(gif, true);
                cell.setFixedHeight(20);
                cell.setHorizontalAlignment(ALIGN_LEFT);
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setBorderColor(new Color(255,255,255));
                cell.setPadding(2);
                tableImg.addCell(cell);

                cell = new PdfPCell();
                cell.setFixedHeight(20);
                cell.setPadding(2);
                cell.setHorizontalAlignment(ALIGN_LEFT);
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setBorderColor(new Color(255,255,255));
                CurPar = "Reliability: ";
                paragraph = new Paragraph(CurPar, font);
                cell.addElement(paragraph);
                tableImg.addCell(cell);

                gif = Img_stars_0;
                if (curOut.getADI() != null)
                    switch ( curOut.getADI().GetAssessmentClass() ) {
                        case ADIndex.INDEX_LOW:
                            gif = Img_stars_1;
                            break;
                        case ADIndex.INDEX_MEDIUM:
                            gif = Img_stars_2;
                            break;
                        case ADIndex.INDEX_HIGH:
                            gif = Img_stars_3;
                            break;
                    }

                cell = new PdfPCell(gif, true);
                cell.setFixedHeight(20);
                cell.setHorizontalAlignment(ALIGN_LEFT);
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setBorderColor(new Color(255,255,255));
                cell.setPadding(2);
                tableImg.addCell(cell);

                tableImg.setWidthPercentage(100);
                tableImg.setHorizontalAlignment(ALIGN_LEFT);
                float[] widthsTbl = {16f, 16f, 16f, 52f};
                tableImg.setWidths(widthsTbl);

            }

            PdfPTable tableAssess = new PdfPTable(1);

            cell = new PdfPCell();
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setVerticalAlignment(ALIGN_TOP);
            cell.setBorderColor(new Color(255,255,255));
            cell.addElement(tableImg);
            tableAssess.addCell(cell);

            cell = new PdfPCell();
            cell.setPadding(2);
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setVerticalAlignment(ALIGN_TOP);
            cell.setBorderColor(new Color(255,255,255));
            CurPar = curOut.getAssessmentVerbose();
            paragraph = new Paragraph(CurPar, fontTitle);
            cell.addElement(paragraph);
            tableAssess.addCell(cell);

            tableAssess.setWidthPercentage(100);

            cell = new PdfPCell();
            cell.addElement(tableAssess);

            cell.setPadding(4);
            cell.setPaddingLeft(6);
            cell.setBorderColor(new Color(200,200,200));
            table.addCell(cell);

            table.setWidthPercentage(100);
            widths[0] = 30f;
            widths[1] = 60f;
            table.setWidths(widths);
            document.add(table);

            /////

            CurPar = "\n" + String.format(StringSelectorCore.getString("report_compound"), inMol.GetId());
            paragraph = new Paragraph(CurPar, font);
            document.add(paragraph);
            CurPar = String.format(StringSelectorCore.getString("report_compound_smiles"), inMol.GetSMILES());
            paragraph = new Paragraph(CurPar, font);
            document.add(paragraph);
            CurPar = StringSelectorCore.getString("report_experimental_value");
            if (ModelWrapper.getModel().GetTrainingSet().hasUnits())
                CurPar += " [" + ModelWrapper.getModel().GetTrainingSet().getUnits() + "]";
            CurPar += ": ";
            if (curOut.HasExperimental()) {
                CurPar += curOut.getExperimentalFormatted();
            } else {
                CurPar += "-";
            }
            paragraph = new Paragraph(CurPar, font);
            document.add(paragraph);

            String[] FormattedResults = curOut.getResults();
            String[] ResultNames = ModelWrapper.getModel().GetResultsName();
            for (int i=0; i<ResultNames.length; i++) {
                if (FormattedResults != null)
                    CurPar = ResultNames[i] + ": " + FormattedResults[i];
                else
                    CurPar = ResultNames[i] + ": -";
                paragraph = new Paragraph(CurPar, font);
                document.add(paragraph);
            }

            if (ModelWrapper.getModel().getInfo().hasAlerts()) {
                if (curOut.getSAList().size() > 0)
                    CurPar = ModelUtilities.BuildSANameList(curOut.getSAList().getSAList());
                else
                    CurPar = "-";
                CurPar = String.format(StringSelectorCore.getString("report_structural_alerts"), CurPar);
                paragraph = new Paragraph(CurPar, font);
                document.add(paragraph);
            }

            if (curOut.getADI() != null)
                CurPar = String.format(StringSelectorCore.getString("report_reliability"), curOut.getADI().GetAssessment());
            else
                CurPar = String.format(StringSelectorCore.getString("report_reliability"), "-");
            paragraph = new Paragraph(CurPar, font);
            document.add(paragraph);
            CurPar = StringSelectorCore.getString("report_remarks");
            paragraph = new Paragraph(CurPar, font);
            document.add(paragraph);

            // Joins different warnings
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i<inMol.GetWarnings().GetSize(); i++)
                msg.append(StringSelectorCore.getString("report_molecule_warning")).append(inMol.GetWarnings().GetMessages(i)).append("\n");
            for (int i = 0; i<inMol.GetErrors().GetSize(); i++)
                msg.append(StringSelectorCore.getString("report_molecule_error")).append(inMol.GetErrors().GetMessages(i)).append("\n");
            if (!curOut.getErrMessage().isEmpty())
                msg.append(StringSelectorCore.getString("report_model_square")).append(curOut.getErrMessage()).append("\n");
            if (msg.length() == 0)
                msg = new StringBuilder(StringSelectorCore.getString("report_non"));
            paragraph = new Paragraph(msg.toString(), font);
            paragraph.setIndentationLeft(8);
            document.add(paragraph);

        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));
        }

    }

    protected void WritePageResults(InsilicoModelConsensusWrapper ModelWrapper, InsilicoMolecule inMol, int molIdx)
            throws GenericFailureException {

        Image gif = null;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        float[] widths = {22f, 78f};


        try {

            document.newPage();
            CurPage++;
//            WritePageHeader(ModelWrapper.getModel().getInfo(), 1);


            //// PAGE 1 - results //////////////////////////////////////////

            InsilicoModelConsensusOutput curOut = ModelWrapper.getResult().get(molIdx);
            String sCAS = "";
            if (!inMol.GetCAS().isEmpty())
                if (!inMol.GetCAS().equals(inMol.GetId()))
                    sCAS =  "(" + inMol.GetCAS() + ")";
            document.add(new Paragraph(String.format(StringSelectorCore.getString("report_prediction_for_compound"), inMol.GetId(), sCAS) +"\n\n", fontTitle));

            table = new PdfPTable(2);


            try {
                gif = Image.getInstance(Depiction.DepictMolecule(inMol, 240, 240),null);
            } catch (Exception e) {
                BufferedImage I = new BufferedImage(240, 240, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = I.createGraphics();
                g.setBackground(new Color(255,255,255));
                g.setColor(new Color(0,0,0));
                g.clearRect(0, 0, 240, 240);
                g.drawLine(20, 20, 220, 220);
                g.drawLine(20, 220, 220, 20);
                g.dispose();
                gif = Image.getInstance(I,null);
            }

            cell = new PdfPCell(gif, true);
            cell.setPadding(2);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(new Color(200,200,200));
            table.addCell(cell);

            PdfPTable tableImg = null;
            String CurPar = "";

            tableImg = new PdfPTable(2);

            cell = new PdfPCell();
            cell.setFixedHeight(20);
            cell.setPadding(2);
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(new Color(255,255,255));
            CurPar = StringSelectorCore.getString("report_prediction_label");
            paragraph = new Paragraph(CurPar, font);
            cell.addElement(paragraph);
            tableImg.addCell(cell);

            switch ( curOut.getAssessmentStatus()) {
                case InsilicoModelOutput.ASSESS_GREEN:
                    gif = Img_circle_green;
                    break;
                case InsilicoModelOutput.ASSESS_RED:
                    gif = Img_circle_red;
                    break;
                case InsilicoModelOutput.ASSESS_YELLOW:
                    gif = Img_circle_yellow;
                    break;
                case InsilicoModelOutput.ASSESS_ORANGE:
                    gif = Img_circle_orange;
                    break;
                case InsilicoModelOutput.ASSESS_GRAY:
                    gif = Img_circle_gray;
                    break;
            }

            cell = new PdfPCell(gif, true);
            cell.setFixedHeight(20);
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPadding(2);
            tableImg.addCell(cell);

            tableImg.setWidthPercentage(100);
            tableImg.setHorizontalAlignment(ALIGN_LEFT);
            float[] widthsTbl = {16f, 84f};
            tableImg.setWidths(widthsTbl);


            PdfPTable tableAssess = new PdfPTable(1);

            cell = new PdfPCell();
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setVerticalAlignment(ALIGN_TOP);
            cell.setBorderColor(new Color(255,255,255));
            cell.addElement(tableImg);
            tableAssess.addCell(cell);

            cell = new PdfPCell();
            cell.setPadding(2);
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setVerticalAlignment(ALIGN_TOP);
            cell.setBorderColor(new Color(255,255,255));
            CurPar = curOut.getAssessmentVerbose();
            paragraph = new Paragraph(CurPar, fontTitle);
            cell.addElement(paragraph);
            tableAssess.addCell(cell);

            tableAssess.setWidthPercentage(100);

            cell = new PdfPCell();
            cell.addElement(tableAssess);

            cell.setPadding(4);
            cell.setPaddingLeft(6);
            cell.setBorderColor(new Color(200,200,200));
            table.addCell(cell);

            table.setWidthPercentage(100);
            widths[0] = 30f;
            widths[1] = 60f;
            table.setWidths(widths);
            document.add(table);

            /////

            CurPar = "\n" + String.format(StringSelectorCore.getString("report_compound"), inMol.GetId());
            paragraph = new Paragraph(CurPar, font);
            document.add(paragraph);
            CurPar = String.format(StringSelectorCore.getString("report_compound_smiles"), inMol.GetSMILES());
            paragraph = new Paragraph(CurPar, font);
            document.add(paragraph);
            CurPar = String.format(StringSelectorCore.getString("report_model_used"), curOut.getUsedModels());
            paragraph = new Paragraph(CurPar, font);
            document.add(paragraph);

            String[] FormattedResults = curOut.getResults();
            String[] ResultNames = ModelWrapper.getModel().GetResultsName();
            for (int i=0; i<ResultNames.length; i++) {
                if (FormattedResults != null)
                    CurPar = ResultNames[i] + ": " + FormattedResults[i];
                else
                    CurPar = ResultNames[i] + ": -";
                paragraph = new Paragraph(CurPar, font);
                document.add(paragraph);
            }

            CurPar = "Remarks: ";
            paragraph = new Paragraph(CurPar, font);
            document.add(paragraph);

            // Joins different warnings
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i<inMol.GetWarnings().GetSize(); i++)
                msg.append(StringSelectorCore.getString("report_molecule_warning")).append(inMol.GetWarnings().GetMessages(i)).append("\n");
            for (int i = 0; i<inMol.GetErrors().GetSize(); i++)
                msg.append(StringSelectorCore.getString("report_molecule_error")).append(inMol.GetErrors().GetMessages(i)).append("\n");
            if (!curOut.getErrMessage().isEmpty())
                msg.append(StringSelectorCore.getString("report_model_square")).append(curOut.getErrMessage()).append("\n");
            if (msg.length() == 0)
                msg = new StringBuilder(StringSelectorCore.getString("report_non"));
            paragraph = new Paragraph(msg.toString(), font);
            paragraph.setIndentationLeft(8);
            document.add(paragraph);

        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));

        }

    }


    protected void WritePageClassBar(InsilicoModelWrapper ModelWrapper, InsilicoMolecule inMol, int molIdx)
            throws GenericFailureException {

        // Check if uncertainty range item is available
        InsilicoModelOutput curOut = ModelWrapper.getResult().get(molIdx);
        Uncertainty UncItem = null;
        for (iReasoningItem R : curOut.getReasoningItem())
            if (R.getReasoningItemType() == InsilicoConstants.REASONING_UNCERTAINTY) {
                UncItem = (Uncertainty) R;
                break;
            }
        if (UncItem == null)
            return;


        Image gif = null;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;

        try {

            ///// PAGE 1 bis - Classification bar ////////////////////////////

            int Classes = UncItem.getBars().size();
            int NumClassPages = (int) Math.ceil(Classes/2);
            int curClassPage = 1;

            for (int cl=0; cl<Classes; cl++) {

                String pages=":";
                if (NumClassPages>1)
                    pages = " - page " + (curClassPage) + " of " + (NumClassPages) + ":";

                if ((cl % 2) == 0) {
                    document.newPage();
                    CurPage++;
                    WritePageHeader(ModelWrapper.getModel().getInfo(), 2);
                    curClassPage++;
                }


                // Chart with class bar
                UncertaintyClassBar CurClass = UncItem.getBars().get(cl);

                document.add(new Paragraph(CurClass.getClassName() + "\n\n", fontTitle));
                document.add(new Paragraph(StringSelectorCore.getString("report_chart_paragraph") + "\n", font));
                document.add(new Paragraph(CurClass.getClassDescription() + "\n\n", font));

                BufferedImage I = new BufferedImage(900, 200, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = I.createGraphics();

                ClassBarChart ch = new ClassBarChart(900, 200);
                ch.AxisName = CurClass.getAxisName();
                for (int k=0; k<CurClass.getThresholds().size(); k++) {
                    ClassBarDataPoint pp = new ClassBarDataPoint(CurClass.getThresholds().get(k),
                            0, CurClass.getThresholdsMarks().get(k));
                    ch.ThresholdPoints.add(pp);
                }
                ch.XPoint = new ClassBarDataPoint(CurClass.getXValue(), 0,
                        CurClass.getXValueAsString(), CurClass.getXValue() +
                        CurClass.getXValueInterval(), 0);

                ch.paintChart(g);

                g.dispose();

                gif = Image.getInstance(I,null);

                table = new PdfPTable(1);
                cell = new PdfPCell(gif, true);
                cell.setHorizontalAlignment(ALIGN_CENTER);
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setBorderColor(new Color(255,255,255));
                cell.setPaddingRight(6);
                table.addCell(cell);

                table.setWidthPercentage(90);
                document.add(table);

            }
            
        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));
        }


    }



    protected void WritePageSimilarMolecules(InsilicoModelWrapper ModelWrapper, InsilicoMolecule inMol, int molIdx)
            throws GenericFailureException {

        Image gif = null;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        float[] widths = {22f, 78f};
        double totTableHeight;

        try {

            document.newPage();
            CurPage++;
            WritePageHeader(ModelWrapper.getModel().getInfo(), 31);
            document.add(new Paragraph("\n", fontSmall));
            totTableHeight = 0;

            ///// PAGE 2 - similar compounds ///////////////////////////////

            InsilicoModelOutput curOut = ModelWrapper.getResult().get(molIdx);
            ArrayList<SimilarMolecule> simMols = curOut.getSimilarMolecules();
            iTrainingSet TS = ModelWrapper.getModel().GetTrainingSet();

            widths[0] = 19f;
            widths[1] = 81f;

            for (int i=0; i<simMols.size(); i++) {

                if (totTableHeight > (645-100)) {
                    document.newPage();
                    CurPage++;
                    WritePageHeader(ModelWrapper.getModel().getInfo(), 31);
                    document.add(new Paragraph("\n", fontSmall));
                    totTableHeight = 0;
                }

                SimilarMolecule curSimMol = simMols.get(i);

                table = new PdfPTable(2);

                try {

                    //todo
//                    if (ModelWrapper.getModel().getInfo().hasTrainingSetPngURL()) {
//                        URL uImg = getClass().getResource(ModelWrapper.getModel().getInfo().getTrainingSetPngURL() + "/" +
//                                TS.getId((int)curSimMol.getIndex()) + ".png" );
//                        gif = Image.getInstance(ImageIO.read(uImg.openStream()),null);
//                    } else {
                        InsilicoMolecule curMol = SmilesMolecule.Convert(TS.getSMILES((int)curSimMol.getIndex()));
//                        InsilicoMolecule curMol = SmilesMolecule.Convert("ciao");

                        gif = Image.getInstance(Depiction.DepictMolecule(curMol, 180, 180),null);
//                    }
                } catch (Throwable e) {
                    BufferedImage I = new BufferedImage(180, 180, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = I.createGraphics();
                    g.setBackground(new Color(255,255,255));
                    g.setColor(new Color(0,0,0));
                    g.clearRect(0, 0, 180, 180);
                    g.drawLine(20, 20, 160, 160);
                    g.drawLine(20, 160, 160, 20);
                    g.dispose();
                    gif = Image.getInstance(I,null);
                    log.warn(String.format(StringSelectorCore.getString("report_depict_mol_error"), curSimMol.getIndex() , ModelWrapper.getModel().getInfo().getName()));
                }

                cell = new PdfPCell(gif, true);
                cell.setBorderColor(new Color(255,255,255));
                cell.setPadding(4);
                table.addCell(cell);

                paragraph = new Paragraph("", font);

                String MolStatus = "";
                if (TS.getMoleculeSet((int)curSimMol.getIndex()) == TrainingSet.MOLECULE_TRAINING)
                    MolStatus = StringSelectorCore.getString("report_molstatus_training");
                if (TS.getMoleculeSet((int)curSimMol.getIndex()) == TrainingSet.MOLECULE_TEST)
                    MolStatus = StringSelectorCore.getString("report_molstatus_test");

                String Units = "";
                if (ModelWrapper.getModel().GetTrainingSet().hasUnits())
                    Units = " [" + ModelWrapper.getModel().GetTrainingSet().getUnits() + "]";

                String SimAlertsInTarget = "";
                String SimAlertsNotInTarget = "";
                if (ModelWrapper.getModel().getInfo().hasAlerts()) {
                    // Retrieve alerts for similar mol
                    String SimMolAlerts = TS.getAlerts((int)curSimMol.getIndex());
                    if (!SimMolAlerts.isEmpty()) {
                        for (String curSA : AlertEncoding.UnpackAlertIds(SimMolAlerts)) {
                            int curSABlock = AlertEncoding.GetBlockIndex(curSA);
                            int curSAIndex = AlertEncoding.GetAlertIndex(curSA);
                            Alert SA = this.SAEngine.GetAlertBlock(curSABlock).getAlerts().get(curSAIndex);

                            boolean AlertInTarget = false;
                            for (Alert a : curOut.getSAList().getSAList())
                                if (a.getId().compareTo(SA.getId()) == 0) {
                                    AlertInTarget = true;
                                    break;
                                }

                            if (AlertInTarget) {
                                if (!SimAlertsInTarget.isEmpty()) SimAlertsInTarget += "; ";
                                SimAlertsInTarget += SA.getName();
                            } else {
                                if (!SimAlertsNotInTarget.isEmpty()) SimAlertsNotInTarget += "; ";
                                SimAlertsNotInTarget += SA.getName();
                            }
                        }
                    }
                }

                paragraph.add(String.format(StringSelectorCore.getString("report_compound_#"), i+1) + "\n\n");
                paragraph.add(String.format(StringSelectorCore.getString("report_cas"), TS.getCAS((int)curSimMol.getIndex())) + "\n");
                paragraph.add(String.format(StringSelectorCore.getString("report_dataset_id"), TS.getId((int)curSimMol.getIndex()), MolStatus) + "\n");
                paragraph.add(String.format(StringSelectorCore.getString("report_smiles"),TS.getSMILES((int)curSimMol.getIndex())) + "\n");
                paragraph.add(String.format(StringSelectorCore.getString("report_similarity"), Format_3D.format(curSimMol.getSimilarity())) + "\n");
                paragraph.add(String.format(StringSelectorCore.getString("report_experimental_valuee"), Units, TS.getExperimentalValueFormatted((int)curSimMol.getIndex())) + "\n");
                paragraph.add(String.format(StringSelectorCore.getString("report_predicted_value"), Units, TS.getPredictedValueFormatted((int)curSimMol.getIndex())) + "\n");
                if (!SimAlertsInTarget.isEmpty())
                    paragraph.add("\n\n" + String.format(StringSelectorCore.getString("report_alert_found"), SimAlertsInTarget) + "");
                if (!SimAlertsNotInTarget.isEmpty())
                    paragraph.add("\n\n" + String.format(StringSelectorCore.getString("report_alert_not_found"), SimAlertsNotInTarget) + "");
                paragraph.add("\n");
                cell = new PdfPCell(new Paragraph(paragraph));
                cell.setBorderColor(new Color(255,255,255));
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setPaddingLeft(6);
                table.addCell(cell);

                table.setWidthPercentage(100);
                table.setWidths(widths);

                PdfPTable table2 = new PdfPTable(1);
                cell = new PdfPCell(table);
                cell.setPadding(2);
                cell.setBorderColor(new Color(200,200,200));
                table2.addCell(cell);
                table2.setWidthPercentage(100);
                table2.setHorizontalAlignment(ALIGN_LEFT);

                table2.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin()) * table2.getWidthPercentage() / 100);
                table2.setLockedWidth(true);
                document.add(table2);

                totTableHeight += table2.getTotalHeight();
            }

        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));
        }

    }


    protected void WritePageAD(InsilicoModelWrapper Model, InsilicoMolecule inMol, int molIdx)
            throws GenericFailureException {

        Image gif = null;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        float[] widths = {22f, 78f};

        InsilicoModelOutput Results = Model.getResult().get(molIdx);

        try {

            document.newPage();
            CurPage++;
            WritePageHeader(Model.getModel().getInfo(), 32);
            document.add(new Paragraph("\n", fontSmall));

            ///// PAGE 3 - AD assessment ///////////////////////////////////

            if (Results.getADIndexSize() > 0 ) {

                // First index - ADI - in a separate table

                table = new PdfPTable(2);
                table.setWidthPercentage(100);
                widths[0] = 5f;
                widths[1] = 95f;
                table.setWidths(widths);

                switch (Results.getADI().GetAssessmentClass()) {
                    case ADIndex.INDEX_HIGH:
                        gif = Img_assessment_good;
                        break;
                    case ADIndex.INDEX_MEDIUM:
                        gif = Img_assessment_non_optimal;
                        break;
                    case ADIndex.INDEX_LOW:
                        gif = Img_assessment_bad;
                        break;
                }

                cell = new PdfPCell(gif, true);
                cell.setHorizontalAlignment(ALIGN_CENTER);
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setBorderColor(new Color(255,255,255));
                cell.setPaddingRight(6);
                table.addCell(cell);

                cell = new PdfPCell();

                paragraph = new Paragraph(Results.getADI().GetIndexNameLong() + "\n", fontTitle);
                cell.addElement(paragraph);
                paragraph = new Paragraph(Results.getADI().GetIndexName() + " = " +
                        Results.getADI().GetIndexValueFormatted() + "\n", font);
                cell.addElement(paragraph);
                paragraph = new Paragraph(String.format(StringSelectorCore.getString("report_explanation"), Results.getADI().GetAssessment()) + "\n", font);
                cell.addElement(paragraph);

                cell.setBorderColor(Color.BLACK);
                cell.setPaddingLeft(8);
                cell.setPaddingBottom(6);
                cell.setVerticalAlignment(ALIGN_TOP);
                table.addCell(cell);

                document.add(table);
                document.add(new Paragraph("\n", fontSmall));

                // All other scores

                table = new PdfPTable(2);
                table.setWidthPercentage(100);
                widths[0] = 5f;
                widths[1] = 95f;
                table.setWidths(widths);

                for (int i=0; i<Results.getADIndexSize(); i++) {

                    iADIndex curAD = Results.getADIndex(i);

                    switch (curAD.GetAssessmentClass()) {
                        case ADIndex.INDEX_HIGH:
                            gif = Img_assessment_good;
                            break;
                        case ADIndex.INDEX_MEDIUM:
                            gif = Img_assessment_non_optimal;
                            break;
                        case ADIndex.INDEX_LOW:
                            gif = Img_assessment_bad;
                            break;
                    }

                    cell = new PdfPCell(gif, true);
                    cell.setHorizontalAlignment(ALIGN_CENTER);
                    cell.setVerticalAlignment(ALIGN_MIDDLE);
                    cell.setBorderColor(new Color(255,255,255));
                    cell.setPaddingRight(6);
                    table.addCell(cell);

                    cell = new PdfPCell();

                    paragraph = new Paragraph(curAD.GetIndexNameLong() + "\n", fontTitle);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(curAD.GetIndexName() + " = " +
                            curAD.GetIndexValueFormatted() + "\n", font);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(String.format(StringSelectorCore.getString("report_explanation"), curAD.GetAssessment()) + ".\n", font);
                    cell.addElement(paragraph);

                    cell.setBorderColor(new Color(200,200,200));
                    cell.setPaddingLeft(8);
                    cell.setPaddingBottom(6);
                    cell.setVerticalAlignment(ALIGN_TOP);
                    table.addCell(cell);
                }

                document.add(table);

            } else {

                document.add(new Paragraph("\n", font));
                document.add(new Paragraph(StringSelectorCore.getString("report_no_ad_available") + "\n\n", font));
                return;

            }

            // Legend

            paragraph = new Paragraph("\n" + StringSelectorCore.getString("report_symbols_explanation") + "\n\n", font);
            document.add(paragraph);

            table = new PdfPTable(2);
            gif = Img_assessment_good;
            cell = new PdfPCell(gif, true);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPaddingRight(6);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph(StringSelectorCore.getString("report_feature_assessment_good"), font);
            cell.addElement(paragraph);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPaddingLeft(4);
            cell.setPaddingBottom(4);
            cell.setVerticalAlignment(ALIGN_TOP);
            table.addCell(cell);
            table.setWidthPercentage(100);
            widths[0] = 5f;
            widths[1] = 95f;
            table.setWidths(widths);

            document.add(table);

            table = new PdfPTable(2);
            gif = Img_assessment_non_optimal;
            cell = new PdfPCell(gif, true);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPaddingRight(4);
            cell.setPaddingBottom(4);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph(StringSelectorCore.getString("report_feature_assessment_non_optimal"), font);
            cell.addElement(paragraph);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPaddingLeft(4);
            cell.setPaddingBottom(4);
            cell.setVerticalAlignment(ALIGN_TOP);
            table.addCell(cell);
            table.setWidthPercentage(100);
            widths[0] = 5f;
            widths[1] = 95f;
            table.setWidths(widths);

            document.add(table);

            table = new PdfPTable(2);
            gif = Img_assessment_bad;
            cell = new PdfPCell(gif, true);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPaddingRight(6);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph(StringSelectorCore.getString("report_feature_assessment_bad"), font);
            cell.addElement(paragraph);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPaddingLeft(4);
            cell.setPaddingBottom(4);
            cell.setVerticalAlignment(ALIGN_TOP);
            table.addCell(cell);
            table.setWidthPercentage(100);
            widths[0] = 5f;
            widths[1] = 95f;
            table.setWidths(widths);

            document.add(table);

        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));
        }

    }



    protected void WritePageSA(InsilicoModelWrapper ModelWrapper, InsilicoMolecule inMol, int molIdx)
            throws GenericFailureException {

        Image gif;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        float[] widths = {22f, 78f};
        double totTableHeight;

        InsilicoModelOutput Results = ModelWrapper.getResult().get(molIdx);
        iTrainingSet TS = ModelWrapper.getModel().GetTrainingSet();

        try {

            ///// PAGE 4 - Fragment analysis ///////////////////////////////

            if ((Results.getSAList() != null) &&
                    (Results.getSAList().size()>0)) {

                int fragPage = 0;
                int totFragPage = Results.getSAList().size();

                for (int fnum=0; fnum<totFragPage; fnum++) {

                    Alert curSA = Results.getSAList().get(fnum);
                    ArrayList<SimilarMolecule> curSim = curSA.GetSimilarMolecules();
                    totTableHeight = 0;

                    document.newPage();
                    CurPage++;
                    WritePageHeader(ModelWrapper.getModel().getInfo(), 41);

                    String pages=":";
                    if (totFragPage>1)
                        pages = " - " + (fragPage+1) + " of " + (totFragPage) + ":";

                    document.add(new Paragraph(String.format(StringSelectorCore.getString("report_reasoning_pages"), inMol.GetId(), pages) + "\n\n", font));
                    document.add(new Paragraph("\n", font));

                    // show fragment

                    try {
                        if (curSA.getImageURL() == null) throw new Exception();
                        URL uImg = getClass().getResource(curSA.getImageURL());
                        gif = Image.getInstance(ImageIO.read(uImg.openStream()),null);
                    } catch (Exception e) {
                        gif = null;
                    }

                    widths[0] = 0f;
                    widths[1] = 100f;
                    table = new PdfPTable(2);
                    cell = new PdfPCell();
                    cell.setBorderColor(new Color(255,255,255));
                    table.addCell(cell);

                    cell = new PdfPCell();

                    paragraph = new Paragraph(String.format(StringSelectorCore.getString("report_fragment_found"),  curSA.getName()) + "\n\n", fontTitle);
                    cell.addElement(paragraph);

                    if (gif != null) {
                        float xy_ratio = gif.getPlainWidth() / gif.getPlainHeight();
                        double xy = xy_ratio / 2.6;
                        float perc_w = 40 * (float)xy;
                        gif.setWidthPercentage(perc_w);
                        gif.setAlignment(ALIGN_LEFT);
                        cell.addElement(gif);
                    }

                    if ((curSim != null) && (!curSim.isEmpty()))  {
                        paragraph = new Paragraph("", font);
                        paragraph.add(curSA.getDescription() + "\n\n");
                        paragraph.add( StringSelectorCore.getString("report_following_similar")+"\n");
                        cell.addElement(paragraph);
                    } else {
                        paragraph = new Paragraph("", font);
                        paragraph.add(curSA.getDescription() + "\n\n");
                        paragraph.add( StringSelectorCore.getString("report_following_similar_none")+"\n");
                        cell.addElement(paragraph);
                    }

                    cell.setBorderColor(new Color(255,255,255));
                    cell.setVerticalAlignment(ALIGN_TOP);
                    cell.setPaddingLeft(6);
                    table.addCell(cell);

                    table.setWidthPercentage(100);
                    table.setWidths(widths);

                    PdfPTable table2 = new PdfPTable(1);
                    cell = new PdfPCell(table);
                    cell.setPadding(2);
                    cell.setPaddingBottom(8);
                    cell.setBorderColor(new Color(200,200,200));
                    table2.addCell(cell);
                    table2.setWidthPercentage(100);
                    table2.setHorizontalAlignment(ALIGN_LEFT);

                    document.add(table2);

                    for (int m=0; m<curSim.size(); m++) {

                        if (totTableHeight > (645-100)) {
                            document.newPage();
                            CurPage++;
                            WritePageHeader(ModelWrapper.getModel().getInfo(), 41);
                            document.add(new Paragraph("\n", fontSmall));
                            totTableHeight = 0;
                        }

                        widths[0] = 19f;
                        widths[1] = 81f;
                        table = new PdfPTable(2);

                        SimilarMolecule curSimMol = curSim.get(m);

                        try {

                            // todo
//                            if (ModelWrapper.getModel().getInfo().hasTrainingSetPngURL()) {
//                                URL uImg = getClass().getResource(ModelWrapper.getModel().getInfo().getTrainingSetPngURL() + "/" +
//                                        TS.getId((int)curSimMol.getIndex()) + ".png" );
//                                gif = Image.getInstance(ImageIO.read(uImg.openStream()),null);
//                            } else {
                                InsilicoMolecule curMol = SmilesMolecule.Convert(TS.getSMILES((int)curSimMol.getIndex()));
                                gif = Image.getInstance(Depiction.DepictMolecule(curMol, 180, 180),null);
//                            }
                        } catch (Exception e) {
                            BufferedImage I = new BufferedImage(180, 180, BufferedImage.TYPE_INT_RGB);
                            Graphics2D g = I.createGraphics();
                            g.setBackground(new Color(255,255,255));
                            g.setColor(new Color(0,0,0));
                            g.clearRect(0, 0, 180, 180);
                            g.drawLine(20, 20, 160, 160);
                            g.drawLine(20, 160, 160, 20);
                            g.dispose();
                            gif = Image.getInstance(I,null);
                            log.warn(String.format(StringSelectorCore.getString("report_depict_mol_error"), curSimMol.getIndex() , ModelWrapper.getModel().getInfo().getName()));
                        }

                        cell = new PdfPCell(gif, true);
                        cell.setBorderColor(new Color(255,255,255));
                        cell.setPadding(4);
                        table.addCell(cell);

                        paragraph = new Paragraph("", font);

                        String MolStatus = "";
                        if (TS.getMoleculeSet((int)curSimMol.getIndex()) == TrainingSet.MOLECULE_TRAINING)
                            MolStatus = StringSelectorCore.getString("report_molstatus_training");
                        if (TS.getMoleculeSet((int)curSimMol.getIndex()) == TrainingSet.MOLECULE_TEST)
                            MolStatus = StringSelectorCore.getString("report_molstatus_test");

                        String Units = "";
                        if (ModelWrapper.getModel().GetTrainingSet().hasUnits())
                            Units = " [" + ModelWrapper.getModel().GetTrainingSet().getUnits() + "]";

                        // Retrieve alerts for similar mol
                        String SimAlertsInTarget = "";
                        String SimAlertsNotInTarget = "";
                        String SimMolAlerts = TS.getAlerts((int)curSimMol.getIndex());
                        if (!SimMolAlerts.isEmpty()) {
                            for (String curSimSA : AlertEncoding.UnpackAlertIds(SimMolAlerts)) {
                                int curSABlock = AlertEncoding.GetBlockIndex(curSimSA);
                                int curSAIndex = AlertEncoding.GetAlertIndex(curSimSA);
                                Alert SA = this.SAEngine.GetAlertBlock(curSABlock).getAlerts().get(curSAIndex);

                                boolean AlertInTarget = false;
                                for (Alert a : Results.getSAList().getSAList())
                                    if (a.getId().compareTo(SA.getId()) == 0) {
                                        AlertInTarget = true;
                                        break;
                                    }

                                if (AlertInTarget) {
                                    if (!SimAlertsInTarget.isEmpty()) SimAlertsInTarget += "; ";
                                    SimAlertsInTarget += SA.getName();
                                } else {
                                    if (!SimAlertsNotInTarget.isEmpty()) SimAlertsNotInTarget += "; ";
                                    SimAlertsNotInTarget += SA.getName();
                                }
                            }
                        }


                        paragraph.add(String.format(StringSelectorCore.getString("report_cas"), TS.getCAS((int)curSimMol.getIndex())) + "\n");
                        paragraph.add(String.format(StringSelectorCore.getString("report_dataset_id"), TS.getId((int)curSimMol.getIndex()), MolStatus) + "\n");
                        paragraph.add(String.format(StringSelectorCore.getString("report_smiles"),TS.getSMILES((int)curSimMol.getIndex())) + "\n");
                        paragraph.add(String.format(StringSelectorCore.getString("report_similarity"), Format_3D.format(curSimMol.getSimilarity())) + "\n\n");
                        paragraph.add(String.format(StringSelectorCore.getString("report_experimental_valuee"), Units, TS.getExperimentalValueFormatted((int)curSimMol.getIndex())) + "\n");
                        paragraph.add(String.format(StringSelectorCore.getString("report_predicted_value"), Units, TS.getPredictedValueFormatted((int)curSim.get(m).getIndex())) + "\n");
                        if (!SimAlertsInTarget.isEmpty())
                            paragraph.add("\n\n" + String.format(StringSelectorCore.getString("report_alert_found"), SimAlertsInTarget) + "");
                        if (!SimAlertsNotInTarget.isEmpty())
                            paragraph.add("\n\n" + String.format(StringSelectorCore.getString("report_alert_not_found"), SimAlertsNotInTarget) + "");
                        paragraph.add("\n");
                        cell = new PdfPCell(new Paragraph(paragraph));
                        cell.setBorderColor(new Color(255,255,255));
                        cell.setVerticalAlignment(ALIGN_MIDDLE);
                        cell.setPaddingLeft(6);
                        table.addCell(cell);

                        table.setWidthPercentage(100);
                        table.setWidths(widths);

                        table2 = new PdfPTable(1);
                        cell = new PdfPCell(table);
                        cell.setPadding(2);
                        cell.setBorderColor(new Color(200,200,200));
                        table2.addCell(cell);
                        table2.setWidthPercentage(100);
                        table2.setHorizontalAlignment(ALIGN_LEFT);

                        document.add(table2);
                        totTableHeight += table2.getTotalHeight();
                    }

                    fragPage++;

                }

            }

        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));
        }

    }



    protected void WritePageDescriptors(InsilicoModelWrapper ModelWrapper, InsilicoMolecule inMol, int molIdx)
            throws GenericFailureException {

        // Check if descriptors item is available
        InsilicoModelOutput curOut = ModelWrapper.getResult().get(molIdx);
        DescriptorAnalysis DescItem = null;
        for (iReasoningItem R : curOut.getReasoningItem())
            if (R.getReasoningItemType() == InsilicoConstants.REASONING_DESCRIPTOR_ANALYSIS) {
                DescItem = (DescriptorAnalysis) R;
                break;
            }
        if (DescItem == null)
            return;


        Image gif = null;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        float[] widths = {22f, 78f};


        try {

            ///// PAGE 5 - Descriptors analysis ////////////////////////////

            document.newPage();
            CurPage++;
            WritePageHeader(ModelWrapper.getModel().getInfo(), 42);

            int DescIdx = DescItem.getDescriptorIndex();
            iTrainingSet TS = ModelWrapper.getModel().GetTrainingSet();

            document.add(new Paragraph(String.format(StringSelectorCore.getString("report_descriptor_name"), DescItem.getDescriptorName()) + "\n", font));
            document.add(new Paragraph(String.format(StringSelectorCore.getString("report_description"),DescItem.getDescription()) + "\n\n", font));


            // Scatter of all values
            document.add(new Paragraph(String.format(StringSelectorCore.getString("report_scatter_all_values"), DescItem.getDescriptorName()) + "\n\n", font));

            BufferedImage I = new BufferedImage(600, 350, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = I.createGraphics();

            ScatterChart ch = new ScatterChart(600, 350);
            ch.BackColorGeneral = Color.WHITE;
            ch.ChartAxis.SetNameX(DescItem.getDescriptorName());
            ch.ChartAxis.SetNameY(DescItem.getExpName());

            ScatterChartDataSet ds1 = new ScatterChartDataSet(ScatterChartDataSet.DS_SCATTER);
            ds1.PointColor = new Color(0x00, 0x99, 0xFF);

            //todo
            for (int k=0; k<TS.getMoleculesSize(); k++)
                ds1.Add(new ScatterChartDataPoint(TS.getDescriptor(k, DescIdx), TS.getExperimentalValue(k)));

            ScatterChartDataSet ds2 = new ScatterChartDataSet(ScatterChartDataSet.DS_SCATTER);
            ds2.PointShape = ScatterChartDataSet.SHAPE_CIRCLE_FILLED;
            ds2.PointColor = Color.RED;
            ds2.Add(new ScatterChartDataPoint(DescItem.getDescriptorValue(), curOut.getMainResultValue()));

            ch.DataSeries.add(ds1);
            ch.DataSeries.add(ds2);

            ch.AdaptAxisScaleToDataset();

            ch.paintChart(g);

            g.dispose();

            gif = Image.getInstance(I,null);

            table = new PdfPTable(1);
            cell = new PdfPCell(gif, true);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPaddingRight(6);
            table.addCell(cell);

            table.setWidthPercentage(75);
            document.add(table);


            // Scatter of similar compounds

            document.add(new Paragraph(String.format(StringSelectorCore.getString("report_scatter_similar_compounds"), DescItem.getDescriptorName()) + "\n\n", font));

            I = new BufferedImage(600, 350, BufferedImage.TYPE_INT_RGB);
            g = I.createGraphics();

            ch = new ScatterChart(600, 350);
            ch.BackColorGeneral = Color.WHITE;
            ch.ChartAxis.SetNameX(DescItem.getDescriptorName());
            ch.ChartAxis.SetNameY(DescItem.getExpName());

            ScatterChartDataSet[] dsSim = new ScatterChartDataSet[3];
            for (int s=0; s<3; s++) {
                SimilarMolecule simMol = curOut.getSimilarMolecules().get(s);
                dsSim[s] = new ScatterChartDataSet(ScatterChartDataSet.DS_SCATTER_WITH_SPANNING);
                dsSim[s].PointShape = ScatterChartDataSet.SHAPE_CIRCLE_FILLED;
                dsSim[s].PointSize = 2 + (int)Math.ceil(Math.pow(16, simMol.getSimilarity()*simMol.getSimilarity()));
                dsSim[s].PointColor = Color.BLACK;
                dsSim[s].PointColorFilling = Color.WHITE;

                //todo 17.11.2020
                dsSim[s].Add(new ScatterChartDataPoint(TS.getDescriptor((int)simMol.getIndex(), DescIdx),
                        TS.getExperimentalValue((int)simMol.getIndex()),
                        TS.getDescriptor((int)simMol.getIndex(), DescIdx),
                        TS.getPredictedValue((int)simMol.getIndex())));
                ch.DataSeries.add(dsSim[s]);
            }

            ds1 = new ScatterChartDataSet(ScatterChartDataSet.DS_SCATTER);
            ds1.PointShape = ScatterChartDataSet.SHAPE_CIRCLE_FILLED;
            ds1.PointSize = 12;
            ds1.PointColor = Color.BLACK;
            ds1.PointColorFilling = Color.RED;
            ds1.Add(new ScatterChartDataPoint(DescItem.getDescriptorValue(), curOut.getMainResultValue()));
            ch.DataSeries.add(ds1);

            ch.AdaptAxisScaleToDataset();

            ch.paintChart(g);

            g.dispose();

            gif = Image.getInstance(I,null);

            table = new PdfPTable(1);
            cell = new PdfPCell(gif, true);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPaddingRight(6);
            table.addCell(cell);

            table.setWidthPercentage(75);
            document.add(table);

            gif.setAlignment(Image.ALIGN_CENTER);
            document.add(gif);


        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));
        }

    }


    protected void WritePageACF(InsilicoModelWrapper ModelWrapper, InsilicoMolecule inMol, int molIdx)
            throws GenericFailureException {

        // Check if descriptors item is available
        InsilicoModelOutput curOut = ModelWrapper.getResult().get(molIdx);
        ACFAnalysis DescItem = null;
        for (iReasoningItem R : curOut.getReasoningItem())
            if (R.getReasoningItemType() == InsilicoConstants.REASONING_ACF_ANALYSIS) {
                DescItem = (ACFAnalysis) R;
                break;
            }
        if (DescItem == null)
            return;


        Image gif = null;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        float[] widths = {14f, 86f};


        try {

            ///// PAGE 4 bis - ACF analysis ////////////////////////////

            int FragmentsPerPage = 7;
            int nFrags = DescItem.getFragments().length;
            int nPages = (int)Math.ceil((double)nFrags / (double)FragmentsPerPage);
            int curPage = 0;

            for (int i=0; i<DescItem.getFragments().length; i++) {

                if (i % FragmentsPerPage == 0) {
                    // set new page
                    curPage++;

                    document.newPage();
                    CurPage++;
                    WritePageHeader(ModelWrapper.getModel().getInfo(), 41);

                    String pages=".";
                    if (nPages>1)
                        pages = " - page " + (curPage) + " of " + (nPages) + ".";
                    document.add(new Paragraph(String.format(StringSelectorCore.getString("report_reasoning_acf"), inMol.GetId(), pages) + "\n", font));
                    document.add(new Paragraph(StringSelectorCore.getString("report_resoning_acf_detail") + "\n\n", font));
                    document.add(new Paragraph("\n", font));
                }


                table = new PdfPTable(2);

                String FragSMILES = DescItem.getFragments()[i];

                try {
                    SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
                    smilesParser.kekulise(false);
                    IAtomContainer CurFragment = smilesParser.parseSmiles(FragSMILES);
                    gif = Image.getInstance(Depiction.DepictMolecule(CurFragment, 100, 100),null);
                    FragSMILES = SmilesMolecule.GenerateSmiles(CurFragment);
                } catch (Exception e) {
                    BufferedImage I = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = I.createGraphics();
                    g.setBackground(new Color(255,255,255));
                    g.setColor(new Color(0,0,0));
                    g.clearRect(0, 0, 100, 100);
                    g.drawLine(10, 10, 90, 90);
                    g.drawLine(10, 90, 90, 10);
                    g.dispose();
                    gif = Image.getInstance(I,null);
                }

                cell = new PdfPCell(gif, true);
                cell.setBorderColor(new Color(255,255,255));
                cell.setPadding(2);
                table.addCell(cell);

                paragraph = new Paragraph("", font);
                paragraph.add(String.format(StringSelectorCore.getString("report_fragment_smiles"), FragSMILES) + "\n");
                if (DescItem.getFragmentsType()[i] == InsilicoConstants.ACF_TYPE_RARE)
                    paragraph.add(MessagesAD.ACF_MESSAGE_RARE + "\n");
                if (DescItem.getFragmentsType()[i] == InsilicoConstants.ACF_TYPE_MISSING)
                    paragraph.add(MessagesAD.ACF_MESSAGE_MISSING + "\n");

                cell = new PdfPCell(new Paragraph(paragraph));
                cell.setBorderColor(new Color(255,255,255));
                cell.setVerticalAlignment(ALIGN_MIDDLE);
                cell.setPaddingLeft(6);
                table.addCell(cell);

                table.setWidthPercentage(100);
                table.setWidths(widths);

                PdfPTable table2 = new PdfPTable(1);
                cell = new PdfPCell(table);
                cell.setPadding(2);
                cell.setBorderColor(new Color(200,200,200));
                table2.addCell(cell);
                table2.setWidthPercentage(100);
                table2.setHorizontalAlignment(ALIGN_LEFT);

                document.add(table2);
            }


        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("guide_generator_error_write_pdf"), e.getMessage()));

        }

    }


}
