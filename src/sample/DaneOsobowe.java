package sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;

import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * Created by pwilkin on 30-Nov-17.
 */
public class DaneOsobowe implements HierarchicalController<MainController> {

    public TextField imie;
    public TextField nazwisko;
    public TextField pesel;
    public TextField indeks;
    public TableView<Student> tabelka;
    private MainController parentController;
    private FileChooser fileChooser;
    private File schowek;

    public void dodaj(ActionEvent actionEvent) {
        Student st = new Student();
        st.setName(imie.getText());
        st.setSurname(nazwisko.getText());
        st.setPesel(pesel.getText());
        st.setIdx(indeks.getText());
        tabelka.getItems().add(st);
    }

    public void setParentController(MainController parentController) {
        this.parentController = parentController;
        //tabelka.getItems().addAll(parentController.getDataContainer().getStudents());
        tabelka.setItems(parentController.getDataContainer().getStudents());
    }

    public void usunZmiany() {
        tabelka.getItems().clear();
        tabelka.getItems().addAll(parentController.getDataContainer().getStudents());
    }

    public MainController getParentController() {
        return parentController;
    }

    public void initialize() {
        for (TableColumn<Student, ?> studentTableColumn : tabelka.getColumns()) {
            if ("imie".equals(studentTableColumn.getId())) {
                studentTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            } else if ("nazwisko".equals(studentTableColumn.getId())) {
                studentTableColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
            } else if ("pesel".equals(studentTableColumn.getId())) {
                studentTableColumn.setCellValueFactory(new PropertyValueFactory<>("pesel"));
            } else if ("indeks".equals(studentTableColumn.getId())) {
                studentTableColumn.setCellValueFactory(new PropertyValueFactory<>("idx"));
            }
        }

        try {

            fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Excel", "*.xlsx")
                    //,new ExtensionFilter("CSV", "*.csv")
                    //,new ExtensionFilter("PDF", "*.pdf")
                    );
            fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));

            File plik = new File(System.getProperty("user.home") + File.separator + "dane.xlsx");
            fileChooser.setInitialDirectory(plik.getParentFile());
            fileChooser.setInitialFileName(plik.getName());

            schowek = new File(System.getProperty("user.home") + File.separator + "schowek_na_ostatni_plik");
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(schowek))) {
                String path = (String) ois.readObject();
                ois.close();
                plik = new File(path);
                fileChooser.setInitialDirectory(plik.getParentFile());
                fileChooser.setInitialFileName(plik.getName());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void zapisz(ActionEvent actionEvent) {
        File plik = fileChooser.showSaveDialog(getStage());
        if (plik != null) {
            if(plik.getName().toLowerCase().endsWith(".xlsx")) {
                zapiszExcel(plik);
            }
        }
    }

    public void wczytaj(ActionEvent actionEvent) {
        File plik = fileChooser.showOpenDialog(getStage());
        if (plik != null) {
            if(plik.getName().toLowerCase().endsWith(".xlsx")) {
                wczytajExcel(plik);
            }
        }
    }

    private void zapiszExcel(File plik) {

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Studenci");

        // Pogrubiony nagłówek,
        // przynajmniej 3 różne kolory:
        // czerwony = brak oceny,
        // źółty = ocena poniżej 3.0,
        // zielony = ocena przynajmniej 3.0

        // https://www.concretepage.com/apache-api/how-to-set-background-and-font-color-in-xlsx-using-poi-in-java

        CellStyle stylBold = wb.createCellStyle();
        CellStyle stylBrakOceny = wb.createCellStyle();
        CellStyle stylNiska = wb.createCellStyle();
        CellStyle stylWystarczy = wb.createCellStyle();

        XSSFFont font;
        font = wb.createFont();
        font.setBold(true);
        stylBold.setFont(font);

        stylBrakOceny.setFillForegroundColor(IndexedColors.RED.getIndex());
        stylNiska.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        stylWystarczy.setFillForegroundColor(IndexedColors.GREEN.getIndex());

        stylBrakOceny.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        stylNiska.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        stylWystarczy.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        stylBrakOceny.setFont(wb.createFont());
        stylBrakOceny.setFont(wb.createFont());
        stylBrakOceny.setFont(wb.createFont());

        XSSFRow h = sheet.createRow(0);
        h.createCell(0).setCellValue("Imię");
        h.createCell(1).setCellValue("Nazwisko");
        h.createCell(2).setCellValue("Ocena");
        h.createCell(3).setCellValue("Opis Oceny");
        h.createCell(4).setCellValue("Index");
        h.createCell(5).setCellValue("PESEL");
        h.setRowStyle(stylBold);

        int row = 1;
        for (Student student : tabelka.getItems()) {
            XSSFRow r = sheet.createRow(row);
            r.createCell(0).setCellValue(student.getName());
            r.createCell(1).setCellValue(student.getSurname());
            r.createCell(3).setCellValue(student.getGradeDetailed());
            r.createCell(4).setCellValue(student.getIdx());
            r.createCell(5).setCellValue(student.getPesel());

            CellStyle styl = stylBrakOceny;
            if (student.getGrade() != null) {
                r.createCell(2).setCellValue(student.getGrade());
                if ( student.getGrade() < 3.0 ) {
                    styl = stylNiska;
                }
                else {
                    styl = stylWystarczy;
                }
            }
            r.setRowStyle(styl);

            row++;
        }

        try (FileOutputStream fos = new FileOutputStream(plik)) {
            wb.write(fos);
            fos.close();
            zapamietajOstatniPlik(plik);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Uwaga na serializację: https://sekurak.pl/java-vs-deserializacja-niezaufanych-danych-i-zdalne-wykonanie-kodu-czesc-i/ */
    private void wczytajExcel(File plik) {
        ArrayList<Student> studentsList = new ArrayList<>();
        try (FileInputStream ois = new FileInputStream(plik)) {
            XSSFWorkbook wb = new XSSFWorkbook(ois);
            XSSFSheet sheet = wb.getSheet("Studenci");
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                Student student = new Student();
                student.setName(row.getCell(0).getStringCellValue());
                student.setSurname(row.getCell(1).getStringCellValue());
                if (row.getCell(2) != null) {
                    student.setGrade(row.getCell(2).getNumericCellValue());
                }
                student.setGradeDetailed(row.getCell(3).getStringCellValue());
                student.setIdx(row.getCell(4).getStringCellValue());
                student.setPesel(row.getCell(5).getStringCellValue());
                studentsList.add(student);
            }
            tabelka.getItems().clear();
            tabelka.getItems().addAll(studentsList);
            ois.close();
            zapamietajOstatniPlik(plik);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void zapamietajOstatniPlik(File plik){
        java.lang.System.out.println("ostatni plik: "+plik.getPath());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(schowek))) {
            oos.writeObject(plik.getAbsolutePath());
            oos.close();
            fileChooser.setInitialDirectory(plik.getParentFile());
            fileChooser.setInitialFileName(plik.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stage getStage(){
        // bez tego można poklikać na okienku pod spodem okienka pokazującego pliki
        return (Stage)imie.getScene().getWindow();
    }
}
