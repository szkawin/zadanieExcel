package sample;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.xssf.usermodel.*;

import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

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

    }

    public void zapisz(ActionEvent actionEvent) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Studenci");

        XSSFCellStyle styl = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        styl.setFont(font);

        XSSFCellStyle stylBold = wb.createCellStyle();
        XSSFFont bold = wb.createFont();
        bold.setBold(true);
        stylBold.setFont(bold);

        XSSFRow nag = sheet.createRow(0);
        nag.createCell(0).setCellValue("Imię");
        nag.createCell(1).setCellValue("Nazwisko");
        nag.createCell(2).setCellValue("Ocena");
        nag.createCell(3).setCellValue("Opis Oceny");
        nag.createCell(4).setCellValue("Index");
        nag.createCell(5).setCellValue("PESEL");
        for(int i=0; i<6; i++) {
            nag.getCell(i).setCellStyle(stylBold);
        }

        int row = 1;
        for (sample.Student student : tabelka.getItems()) {
            XSSFRow r = sheet.createRow(row);
            r.setRowStyle(styl);
            r.createCell(0).setCellValue(student.getName());
            r.createCell(1).setCellValue(student.getSurname());
            if (student.getGrade() != null) {
                r.createCell(2).setCellValue(student.getGrade());
            }
            r.createCell(3).setCellValue(student.getGradeDetailed());
            r.createCell(4).setCellValue(student.getIdx());
            r.createCell(5).setCellValue(student.getPesel());
            row++;
        }
        try (FileOutputStream fos = new FileOutputStream("data.xlsx")) {
            wb.write(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Uwaga na serializację: https://sekurak.pl/java-vs-deserializacja-niezaufanych-danych-i-zdalne-wykonanie-kodu-czesc-i/ */
    public void wczytaj(ActionEvent actionEvent) {
        ArrayList<sample.Student> studentsList = new ArrayList<>();
        try (FileInputStream ois = new FileInputStream("data.xlsx")) {
            XSSFWorkbook wb = new XSSFWorkbook(ois);
            XSSFSheet sheet = wb.getSheet("Studenci");
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                sample.Student student = new sample.Student();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
