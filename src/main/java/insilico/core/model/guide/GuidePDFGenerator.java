package insilico.core.model.guide;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import insilico.core.alerts.AlertsEngine;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.InsilicoModel;
import insilico.core.model.InsilicoModelInfoUpdated;
import insilico.core.model.runner.InsilicoModelConsensusWrapper;
import insilico.core.model.runner.InsilicoModelWrapper;
import insilico.core.version.InsilicoInfo;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;

import static com.lowagie.text.Element.*;
import static com.lowagie.text.Element.ALIGN_MIDDLE;
import static com.lowagie.text.Rectangle.BOTTOM;

@Slf4j
public class GuidePDFGenerator {

    protected Document document;
    protected FileOutputStream doc_bos;
    protected PdfWriter writer;
    protected ArrayList<InsilicoModelWrapper> modelWrappers;
    protected ArrayList<InsilicoModelConsensusWrapper> modelConsWrappers;
    protected boolean HiResMode;
    protected int CurPage;

    // For alerts in similar mols
    private final AlertsEngine SAEngine;

    // Fonts object
    private final static int DEFAULT_FONT = Font.TIMES_ROMAN;
    private Font font;
    private Font fontGray;
    private Font fontTitle;
    private Font fontTitleGreen;
    private Font fontBig;
    private Font fontSmall;
    private Font fontHeader;
    private Font fontBigUnderline;
    private Font fontBold;
    private Font fontLink;
    private Font fontReference;

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

    // Core version
    private InsilicoInfo coreVersion;

    private InsilicoModelInfoUpdated modelInfo;

    // Format object
    private DecimalFormat Format_3D;


    // CONSTRUCTOR
    public GuidePDFGenerator(boolean HiRes, InsilicoModel model) throws InitFailureException {
        this.HiResMode = HiRes;
        FetchImageObjects(HiRes);
        CreateFonts();

        DecimalFormatSymbols InternationalSymbols =
                new DecimalFormatSymbols();
        InternationalSymbols.setDecimalSeparator('.');

        Format_3D = new DecimalFormat("0.###", InternationalSymbols);

        modelInfo = model.getInfo();
        SAEngine = new AlertsEngine();

        InitReport(modelInfo);
    }

    // Init Images from files
    private void FetchImageObjects(boolean HiRes) throws InitFailureException {

        // Init images
        try {
            URL uImage;

//            uImage = new File("images/light_green.png").toURI().toURL();
            uImage = getClass().getClassLoader().getResource("images/light_green.png" );
            Img_circle_green = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/light_yellow.png" );
            Img_circle_yellow = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/light_orange.png" );
            Img_circle_orange = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/light_red.png" );
            Img_circle_red = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/light_gray.png" );
            Img_circle_gray = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/stars_0.png" );
            Img_stars_0 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/stars_1.png" );
            Img_stars_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/stars_2.png" );
            Img_stars_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/stars_3.png" );
            Img_stars_3 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/assessment_ok.png" );
            Img_assessment_good = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/assessment_bad.png" );
            Img_assessment_bad = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/assessment_non-optimal.png" );
            Img_assessment_non_optimal = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            uImage = getClass().getClassLoader().getResource("images/vega_header.png" );
            Img_header = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            if (HiRes) {

                uImage = this.getClass().getClassLoader().getResource("images/hi_report_top_section_1.png" );
                Img_title_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

                uImage = getClass().getClassLoader().getResource("images/hi_report_top_section_2.png" );
                Img_title_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

                uImage = getClass().getClassLoader().getResource("images/hi_report_top_section_3_1.png" );
                Img_title_3_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

                uImage = getClass().getClassLoader().getResource("images/hi_report_top_section_3_2.png" );
                Img_title_3_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

                uImage = getClass().getClassLoader().getResource("images/hi_report_top_section_4_1.png" );
                Img_title_4_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

                uImage = getClass().getClassLoader().getResource("images/hi_report_top_section_4_2.png" );
                Img_title_4_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            } else {

                uImage = getClass().getClassLoader().getResource("images/report_top_section_1.png" );
                Img_title_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

                uImage = getClass().getClassLoader().getResource("images/report_top_section_2.png" );
                Img_title_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

                uImage = getClass().getClassLoader().getResource("images/report_top_section_3_1.png" );
                Img_title_3_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

                uImage = getClass().getClassLoader().getResource("images/report_top_section_3_2.png" );
                Img_title_3_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

                uImage = getClass().getClassLoader().getResource("images/report_top_section_4_1.png" );
                Img_title_4_1 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

                uImage = getClass().getClassLoader().getResource("images/report_top_section_4_2.png" );
                Img_title_4_2 = Image.getInstance(ImageIO.read(uImage.openStream()),null);

            }
        } catch (Exception e) {
            throw new InitFailureException("Unable to load images");
        }
    }

    private void CreateFonts() {
        // Creates fonts
        font = new Font(DEFAULT_FONT, 10, Font.NORMAL);
        fontBold = new Font(DEFAULT_FONT, 11, Font.BOLD);
        fontReference = new Font(DEFAULT_FONT, 10, Font.BOLDITALIC);

        fontLink = new Font(DEFAULT_FONT, 10, Font.BOLD + Font.UNDERLINE);
        fontLink.setColor(Color.BLUE);

        fontGray = new Font(DEFAULT_FONT, 10, Font.NORMAL);
        fontGray.setColor(Color.GRAY);
        fontTitle = new Font(DEFAULT_FONT, 10, Font.BOLD);
        fontTitleGreen = new Font(DEFAULT_FONT, 10, Font.BOLD);
        fontTitleGreen.setColor(new Color(0,136,0));
        fontBig = new Font(DEFAULT_FONT, 14, Font.BOLD);
        fontBigUnderline = new Font(DEFAULT_FONT, 14, Font.BOLD + Font.UNDERLINE);
        fontSmall = new Font(DEFAULT_FONT, 4, Font.NORMAL);
        fontHeader = new Font(DEFAULT_FONT, 18, Font.BOLD);

    }

    protected void InitReport(InsilicoModelInfoUpdated modelInfo) throws InitFailureException {

        this.modelInfo = modelInfo;


        try {
            document = new Document(PageSize.A4);
            doc_bos = new FileOutputStream(modelInfo.getName() + ".pdf");
            writer = PdfWriter.getInstance(document, doc_bos);

            document.open();
            GenerateReport();
        } catch (Exception e) {
            throw new InitFailureException("Unable to create PDF document (" + e.getMessage() + ")");
        }

    }

    private void GenerateReport() throws GenericFailureException {

        document.setMarginMirroring(true);
        document.setMarginMirroringTopBottom(true);
        document.setMargins(50, 50, 25, 25);
        AddCover();
        WritePageGuide();
        document.close();
    }



    private void WritePageHeader(String headerName) throws GenericFailureException {

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
//            gif = Img_header;
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
            paragraph = new Paragraph(headerName , fontHeader);
            cell = new PdfPCell(paragraph);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setFixedHeight(30);
            cell.setBorder(BOTTOM);
            cell.setBorderColor(new Color(20,20,20));
            table.addCell(cell);
            paragraph = new Paragraph("page ", font);
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

            float[] widths_title = {100f};
            table = new PdfPTable(1);
            cell = new PdfPCell(gif, true);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setVerticalAlignment(ALIGN_MIDDLE);
            cell.setBorderColor(Color.WHITE);
            table.addCell(cell);
            table.setTotalWidth(w);
            table.setWidths(widths_title);
            document.add(new Paragraph("\n\n\n", font));


        } catch (Exception e) {
            throw new GenericFailureException("Error while writing PDF report (" + e.getMessage() + ")");
        }
    }

    protected void AddCover() throws GenericFailureException {

        Image gif;
        PdfPTable table;
        PdfPCell cell;
        Paragraph paragraph;
        float w = PageSize.A4.getWidth();
        float h = PageSize.A4.getHeight();

        try {

            double y_pos = 0;

////             Header
            URL uBanner = null;
//            if (HiResMode)
//                uBanner = getClass().getClassLoader().getResource("images/vega_header.png");
//            else
//                uBanner = getClass().getClassLoader().getResource("images/vega_header.png");
//            gif = Image.getInstance(ImageIO.read(uBanner.openStream()),null);
            table = new PdfPTable(1);
            cell = new PdfPCell();
            cell.setPaddingBottom(4);
            cell.setHorizontalAlignment(ALIGN_CENTER);
            cell.setBorderColor(new Color(255,255,255));
            table.addCell(cell);
            table.setTotalWidth(w-20);
            y_pos = table.writeSelectedRows(0, -1, 10, h-10, writer.getDirectContent());
//
//             Footer
//            if (HiResMode)
//                uBanner = getClass().getClassLoader().getResource("images/hi_vega_report_cover_bottom.png" );
//            else
//                uBanner = getClass().getClassLoader().getResource("images/vega_report_cover_bottom.png" );
//            gif = Image.getInstance(ImageIO.read(uBanner.openStream()),null);
//            table = new PdfPTable(1);
//            cell = new PdfPCell();
//            cell.setHorizontalAlignment(ALIGN_CENTER);
//            cell.setBorderColor(new Color(255,255,255));
//            table.addCell(cell);
//            table.setTotalWidth(w-20);
//            int GifHeight = Math.round((w-20) * (gif.getHeight() / gif.getWidth()));
//            table.writeSelectedRows(0, -1, 10, 10 + GifHeight, writer.getDirectContent());

            // Mid contents
            long MidTable_Left = Math.round(10 + (w-20) * 0.096);
            long MidTable_Width = Math.round((w-20) * 0.858);
            table = new PdfPTable(1);
            cell = new PdfPCell();
            cell.setPaddingTop(100);
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setBorderColor(new Color(255,255,255));
            cell.setPadding(25);
            cell.setBackgroundColor(new Color(240,240,240));
            StringBuilder CurPar = new StringBuilder("Guide for model:\n\n");
            paragraph = new Paragraph(CurPar.toString(), fontTitle);
            cell.addElement(paragraph);
            CurPar = new StringBuilder();
            CurPar.append(modelInfo.getName()).append("\n");
            CurPar.append(modelInfo.getSummary()).append("\n");

            CurPar.append("\n\n");
            paragraph = new Paragraph(CurPar.toString(), fontBig);
            cell.addElement(paragraph);
            CurPar = new StringBuilder("Version: " + modelInfo.getVersion());
            paragraph = new Paragraph(CurPar.toString(), fontGray);
            cell.addElement(paragraph);
            table.addCell(cell);
            table.setTotalWidth(MidTable_Width);
            table.writeSelectedRows(0, -1, MidTable_Left, Math.round(y_pos) - 5 , writer.getDirectContent());

        } catch (Exception e) {
            throw new GenericFailureException("Error while writing PDF report (" + e.getMessage() + ")");
        }

        CurPage = 0;
    }

    protected void WritePageGuide() throws GenericFailureException {


        try {
            document.newPage();
            CurPage++;
            int index = 1;
            WritePageHeader("1. MODEL EXPLANATION");


            // 1.1 Introduction
            PdfPCell cell = new PdfPCell();
            cell.setHorizontalAlignment(ALIGN_LEFT);
            Paragraph sectionTitle = new Paragraph("1." + index + "\t INTRODUCTION", fontBigUnderline);
            sectionTitle.setIndentationLeft(1);
            document.add(sectionTitle);
            document.add(new Paragraph("\n", font));

            Paragraph sectionBody = new Paragraph(modelInfo.Guide.get("Description"), font);
            document.add(sectionBody);
            document.add(new Paragraph("\n\n\n", font));
            index++;

            // 1.2 Details
            sectionTitle = new Paragraph("1." + index + " \t MODEL DETAILS", fontBigUnderline);
            sectionTitle.setIndentationLeft(1);
            document.add(sectionTitle);
            document.add(new Paragraph("\n", font));

            sectionBody = new Paragraph(modelInfo.Guide.get("Model"), font);
            document.add(sectionBody);
            document.add(new Paragraph("\n", font));
            Paragraph endpointModel = new Paragraph(modelInfo.Guide.get("Descriptors"), font);
            document.add(endpointModel);
            document.add(new Paragraph("\n\n\n", font));
            index++;

            // 1.3 Applicability Domain
            sectionTitle = new Paragraph("1." + index + " \t APPLICABILITY DOMAIN", fontBigUnderline);
            sectionTitle.setIndentationLeft(1);
            document.add(sectionTitle);

            document.add(new Paragraph("\n", font));
            document.add(new Paragraph(TextConstants.AD_SECTION_INTRO, font));
            document.add(new Paragraph("\n", font));

            for (HashMap<String, String> AD : modelInfo.Applicability_Domain) {

                switch (AD.get("Name")) {
                    case "Similar_Molecules":
                        document.add(new Paragraph(TextConstants.AD_SIMILAR_MOLECULES_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_SIMILAR_MOLECULES_INTRO, font));
                        break;
                    case "Accuracy_DM":
                        document.add(new Paragraph(TextConstants.AD_ACCURACY_DM_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_ACCURACY_DM_INTRO, font));
                        break;
                    case "Accuracy":
                        document.add(new Paragraph(TextConstants.AD_ACCURACY_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_ACCURACY_INTRO, font));
                        break;
                    case "Concordance":
                        document.add(new Paragraph(TextConstants.AD_CONCORDANCE_TITLE, fontBold));
                        document.add(new Paragraph("N° Similar Molecules: " + modelInfo.getSimilarMolsValue(), font));
                        document.add(new Paragraph(TextConstants.AD_CONCORDANCE_INTRO, font));
                        break;
                    case "Reliability":
                        document.add(new Paragraph(TextConstants.AD_RELIABILITY_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_RELIABILITY_INTRO, font));
                        break;
                    case "Maximum_Error":
                        document.add(new Paragraph(TextConstants.AD_MAXIMUM_ERROR_PREDICTION_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_MAXIMUM_ERROR_PREDICTION_INTRO, font));
                        break;
                    case "ACF_SimCheck":
                        document.add(new Paragraph(TextConstants.AD_ACF_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_ACF_INTRO, font));
                        break;
                    case "NMN_concordance":
                        document.add(new Paragraph(TextConstants.AD_NNM_CONCORDANCE_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_NNM_CONCORDANCE_INTRO, font));
                        break;
                    case "Model_Descriptors_Range_Check":
                        document.add(new Paragraph(TextConstants.AD_DESCRIPTORS_RANGE_CHECK_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_DESCRIPTORS_RANGE_CHECK_INTRO, font));
                        break;
                    case "AD_Index":
                        document.add(new Paragraph(TextConstants.AD_GLOBAL_INDEX_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_GLOBAL_INDEX_INTRO, font));
                        break;
                    case "SA_Concordance":
                        document.add(new Paragraph(TextConstants.AD_STRUCTURAL_ALERT_CONCORDANCE_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_STRUCTURAL_ALERT_CONCORDANCE_INTRO, font));

                    case "LogP_reliability":
                        document.add(new Paragraph(TextConstants.AD_LOGP_RELIABILITY_TITLE, fontBold));
                        document.add(new Paragraph(TextConstants.AD_LOGP_RELIABILITY_INTRO, font));
                }
                document.add(new Paragraph("\n", font));

                PdfPTable table;
                float[] widths = {22f, 78f};
                table = new PdfPTable(widths);
                table.setWidthPercentage(100);
                table.setWidths(widths);
//                cell.setPaddingBottom(2);
                cell.setPaddingLeft(5);
                cell.setPaddingRight(3);
                cell.setExtraParagraphSpace(2);

                if(AD.get(InsilicoModelInfoUpdated.Guide_AD_RangeTop) != null) {
                    cell = new PdfPCell(new Paragraph(AD.get(InsilicoModelInfoUpdated.Guide_AD_RangeTop), font));
                    table.addCell(cell);
                    cell = new PdfPCell(new Paragraph(AD.get(InsilicoModelInfoUpdated.Guide_AD_DescriptionRangeTop), font));
                    table.addCell(cell);
                }

                if(AD.get(InsilicoModelInfoUpdated.Guide_AD_RangeMid) != null){
                    cell = new PdfPCell(new Paragraph(AD.get(InsilicoModelInfoUpdated.Guide_AD_RangeMid), font));
                    table.addCell(cell);
                    cell = new PdfPCell(new Paragraph(AD.get(InsilicoModelInfoUpdated.Guide_AD_DescriptionRangeMid), font));
                    table.addCell(cell);
                }

                if(AD.get(InsilicoModelInfoUpdated.Guide_AD_RangeBottom) != null) {
                    cell = new PdfPCell(new Paragraph(AD.get(InsilicoModelInfoUpdated.Guide_AD_RangeBottom), font));
                    table.addCell(cell);
                    cell = new PdfPCell(new Paragraph(AD.get(InsilicoModelInfoUpdated.Guide_AD_DescriptionRangeBottom), font));
                    table.addCell(cell);
                }

                if(AD.get(InsilicoModelInfoUpdated.Guide_AD_RangeMidBottom) != null) {
                    cell = new PdfPCell(new Paragraph(AD.get(InsilicoModelInfoUpdated.Guide_AD_RangeMidBottom), font));
                    table.addCell(cell);
                    cell = new PdfPCell(new Paragraph(AD.get(InsilicoModelInfoUpdated.Guide_AD_DescriptionRangeMidBottom), font));
                    table.addCell(cell);
                }

                if(AD.get(InsilicoModelInfoUpdated.Guide_AD_RangeMidTop) != null) {
                    cell = new PdfPCell(new Paragraph(AD.get(InsilicoModelInfoUpdated.Guide_AD_RangeMidTop), font));
                    table.addCell(cell);
                    cell = new PdfPCell(new Paragraph(AD.get(InsilicoModelInfoUpdated.Guide_AD_DescriptionRangeMidTop), font));
                    table.addCell(cell);
                }
                document.add(table);

                document.add(new Paragraph("\n",font));
            }

            // 1.4 Structural Alerts
            if(modelInfo.hasAlerts()){
                sectionTitle = new Paragraph("1." + index + "\t STRUCTURAL ALERTS", fontBigUnderline);
                sectionTitle.setIndentationLeft(1);
                document.add(sectionTitle);
                document.add(new Paragraph("\n", font));
//                document.add(new Paragraph())
                sectionBody = new Paragraph();
                sectionBody.add(new Paragraph(modelInfo.getGuide().get("Structural_Alerts"), font));
                document.add(sectionBody);
                document.add(new Paragraph("\n"));
                index++;
            }


            // 1.4 References
            sectionTitle = new Paragraph("1." + index + "\t REFERENCES", fontBigUnderline);
            sectionTitle.setIndentationLeft(1);
            document.add(sectionTitle);
            document.add(new Paragraph("\n"));

            sectionBody = new Paragraph("QMRF Link: ", fontBold);
            sectionBody.add(new Paragraph(modelInfo.Reference.getQMRFLink(), fontLink));
            document.add(sectionBody);
            document.add(new Paragraph("\n"));


            int curRef = 1;
            for(HashMap<String, String> singleRef : modelInfo.Reference.getReferenceList()) {
                document.add(new Paragraph("#" + curRef + "\n", font));
                document.add(new Paragraph(singleRef.get(InsilicoModelInfoUpdated.Reference_ReferenceName), fontReference));
                document.add(new Paragraph(singleRef.get(InsilicoModelInfoUpdated.Reference_ReferenceLink), fontLink));
                document.add(new Paragraph("\n"));
                curRef++;
            }
            index++;

            // INSERT ALERT HERE
            if(modelInfo.hasAlerts()){
            }

            sectionTitle = new Paragraph("1." + index + "\t MODEL STATISTICS", fontBigUnderline);
            sectionTitle.add("\n");
            sectionTitle.setIndentationLeft(1);
            document.add(sectionTitle);
            document.add(new Paragraph(TextConstants.STATS_INTRO, font));

            // Training Set stats
            sectionBody = new Paragraph("- Training Set: " + "\n", fontBold);
            document.add(sectionBody);

            sectionBody = new Paragraph("n = " + modelInfo.Stats.get(InsilicoModelInfoUpdated.Stats_n_Train) + "        ", font);
            sectionBody.add("\t" + "R2 = " + modelInfo.Stats.get(InsilicoModelInfoUpdated.Stats_R2_Train)+ "        ");
            sectionBody.add("\t" + "RMSE = " + modelInfo.Stats.get(InsilicoModelInfoUpdated.Stats_RMSE_Train) + "        ");

            document.add(sectionBody);

            // Test Set stats
            sectionBody = new Paragraph("- Test Set: " + "\n", fontBold);
            document.add(sectionBody);

            sectionBody = new Paragraph("n = " + modelInfo.Stats.get(InsilicoModelInfoUpdated.Stats_n_Test) + "        ", font);
            sectionBody.add("\t" + "R2 = " + modelInfo.Stats.get(InsilicoModelInfoUpdated.Stats_R2_Test) + "        ");
            sectionBody.add("\t" + "RMSE = " + modelInfo.Stats.get(InsilicoModelInfoUpdated.Stats_RMSE_Test) + "        ");

            document.add(sectionBody);

        } catch (Exception e) {
            throw new GenericFailureException("Error while writing PDF report (" + e.getMessage() + ")");
        }
    }








}
