import net.sf.jasperreports.engine.JRCommonText;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.data.*;
import net.sf.jasperreports.engine.util.DefaultFormatFactory;
import net.sf.jasperreports.engine.util.JRStringUtil;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.export.XlsReportConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellReference;

import java.util.Date;

/**
 * CustomJRXlsExporter
 *
 * @author Victoria_Chernenko
 */
public class CustomJRXlsExporter extends JRXlsExporter {
    private static final Log log = LogFactory.getLog(JRXlsExporter.class);
    private static final String TEXT_FORMAT = "@";

    protected void createTextCell(final JRPrintText textElement, final JRExporterGridCell gridCell, final int colIndex, final int rowIndex, final JRStyledText styledText, final StyleInfo baseStyle, final short forecolor) throws JRException
    {
        String formula = getFormula(textElement);
        String textStr = styledText.getText();
        if (formula != null)
        {
            try
            {
                TextValue value = getTextValue(textElement, textStr);

                if (value instanceof NumberTextValue)
                {
                    String convertedPattern = getConvertedPattern(textElement, ((NumberTextValue)value).getPattern());
                    if (convertedPattern != null)
                    {
                        baseStyle.setDataFormat(
                                dataFormat.getFormat(convertedPattern)
                        );
                    }
                }
                else if (value instanceof DateTextValue)
                {
                    String convertedPattern = getConvertedPattern(textElement, ((DateTextValue)value).getPattern());
                    if (convertedPattern != null)
                    {
                        baseStyle.setDataFormat(
                                dataFormat.getFormat(convertedPattern)
                        );
                    }
                }

                HSSFCellStyle cellStyle = initCreateCell(gridCell, colIndex, rowIndex, baseStyle);
                cell.setCellType(CellType.FORMULA);

                // the formula text will be stored in formulaCellsMap in order to be applied only after
                // all defined names are created and available in the workbook (see #closeWorkbook())
                formulaCellsMap.put(cell, formula);
                endCreateCell(cellStyle);
                return;
            }
            catch(Exception e)//FIXMENOW what exceptions could we get here?
            {
                if(log.isWarnEnabled())
                {
                    log.warn(e.getMessage());
                }
            }
        }

        XlsReportConfiguration configuration = getCurrentItemConfiguration();

        if (configuration.isDetectCellType())
        {
            TextValue value = getTextValue(textElement, textStr);
            value.handle(new TextValueHandler()
            {
                @Override
                public void handle(StringTextValue textValue)
                {
                    if(TEXT_FORMAT.equals(getConvertedPattern(textElement, null)))
                    {
                        //set cell type as Text
                        baseStyle.setDataFormat(dataFormat.getFormat(TEXT_FORMAT));
                    }
                    HSSFCellStyle cellStyle = initCreateCell(gridCell, colIndex, rowIndex, baseStyle);
                    if (textValue.getText() == null || textValue.getText().length() == 0)
                    {
                        cell.setCellType(CellType.BLANK);
                    }
                    else
                    {
                        if (JRCommonText.MARKUP_NONE.equals(textElement.getMarkup()) || isIgnoreTextFormatting(textElement))
                        {
                            setStringCellValue(textValue.getText());
                        }
                        else
                        {
                            setRichTextStringCellValue(styledText, forecolor, textElement, getTextLocale(textElement));
                        }
                    }
                    endCreateCell(cellStyle);
                }

                @Override
                public void handle(NumberTextValue textValue)
                {
                    String convertedPattern = getConvertedPattern(textElement, textValue.getPattern());
                    if (convertedPattern != null)
                    {
                        //FIXME: use localized Excel pattern
                        baseStyle.setDataFormat(
                                dataFormat.getFormat(convertedPattern)
                        );
                    }

                    HSSFCellStyle cellStyle = initCreateCell(gridCell, colIndex, rowIndex, baseStyle);
                    if (textValue.getValue() == null)
                    {
                        cell.setCellType(CellType.BLANK);
                    }
                    else
                    {
                        double doubleValue = textValue.getValue().doubleValue();
                        if (DefaultFormatFactory.STANDARD_NUMBER_FORMAT_DURATION.equals(convertedPattern))
                        {
                            doubleValue = doubleValue / 86400;
                        }
                        cell.setCellValue(doubleValue);
                    }
                    endCreateCell(cellStyle);
                }

                @Override
                public void handle(DateTextValue textValue)
                {
                    String convertedPattern = getConvertedPattern(textElement, textValue.getPattern());
                    if (convertedPattern != null)
                    {
                        //FIXME: use localized Excel pattern
                        baseStyle.setDataFormat(
                                dataFormat.getFormat(convertedPattern)
                        );
                    }
                    HSSFCellStyle cellStyle = initCreateCell(gridCell, colIndex, rowIndex, baseStyle);
                    Date date = textValue.getValue();
                    if (date == null)
                    {
                        cell.setCellType(CellType.BLANK);
                    }
                    else
                    {
                        date = translateDateValue(textElement, date);
                        cell.setCellValue(date);
                    }
                    endCreateCell(cellStyle);
                }

                @Override
                public void handle(BooleanTextValue textValue)
                {
                    HSSFCellStyle cellStyle = initCreateCell(gridCell, colIndex, rowIndex, baseStyle);
                    if (textValue.getValue() == null)
                    {
                        cell.setCellType(CellType.BLANK);
                    }
                    else
                    {
                        cell.setCellValue(textValue.getValue());
                    }
                    endCreateCell(cellStyle);
                }
            });
        }
        else
        {
            HSSFCellStyle cellStyle = initCreateCell(gridCell, colIndex, rowIndex, baseStyle);
            if (JRCommonText.MARKUP_NONE.equals(textElement.getMarkup()) || isIgnoreTextFormatting(textElement))
            {
                setStringCellValue(textStr);
            }
            else
            {
                setRichTextStringCellValue(styledText, forecolor, textElement, getTextLocale(textElement));
            }
            endCreateCell(cellStyle);
        }

        if(!configuration.isIgnoreAnchors())
        {
            String anchorName = textElement.getAnchorName();
            if(anchorName != null)
            {
                HSSFName aName = workbook.createName();
                aName.setNameName(JRStringUtil.getJavaIdentifier(anchorName));
                aName.setSheetIndex(workbook.getSheetIndex(sheet));
                org.apache.poi.ss.util.CellReference cRef = new CellReference(rowIndex, colIndex, true, true);
                aName.setRefersToFormula(cRef.formatAsString());
                anchorNames.put(anchorName, aName);
            }
        }

        setHyperlinkCell(textElement);
    }
}
