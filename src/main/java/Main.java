import com.github.javafaker.Faker;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.export.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Main
 *
 * @author Victoria_Chernenko
 */
public class Main {
    public static void main(String[] args) {
        try {
            generateReport();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateReport()throws Exception {

        try (InputStream templateStream = Main.class.getResourceAsStream("/Leaf_Red.jrxml")) {

            final JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

            final Map<String, Object> parameters = new HashMap<>();
            parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);

            final JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(getDataBeanList());

            final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
            configuration.setOnePagePerSheet(true);
            configuration.setDetectCellType(true); // Detect cell types 
            configuration.setWhitePageBackground(false); // No white background!
            configuration.setFontSizeFixEnabled(false);

            configuration.setRemoveEmptySpaceBetweenRows(true);
            configuration.setRemoveEmptySpaceBetweenColumns(true);

            final Exporter exporter = new CustomJRXlsExporter();

            exporter.setConfiguration(configuration);
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));


            try (FileOutputStream fos = new FileOutputStream("Leaf_Red.xls");
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                OutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(baos);
                exporter.setExporterOutput(exporterOutput);
                exporter.exportReport();

                baos.writeTo(fos);
            }
        }
    }

    public static ArrayList<DataBean> getDataBeanList() {
        Faker faker = new Faker();
        Random random = new Random();
        ArrayList<DataBean> dataBeanList = new ArrayList<DataBean>();
        for (int i = 0; i < 1; i++) {
            dataBeanList.add(new DataBean(faker.name().firstName(), faker.name().lastName(),
                    faker.phoneNumber().cellPhone(), faker.date().birthday(), random.nextInt(10)));
        }
        return dataBeanList;
    }
}
