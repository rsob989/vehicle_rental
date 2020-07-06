package pl.carrental.app;

import pl.carrental.app.options.AddOptions;
import pl.carrental.app.options.DeleteOptions;
import pl.carrental.app.options.ShowOptions;
import pl.carrental.exceptions.*;
import pl.carrental.io.ConsolePrinter;
import pl.carrental.io.DataReader;
import pl.carrental.io.file.FileManager;
import pl.carrental.io.file.FileManagerBuilder;
import pl.carrental.model.*;
import java.util.Comparator;
import java.util.InputMismatchException;

public class RentalControl {
    private ConsolePrinter cp = new ConsolePrinter();
    private DataReader dr = new DataReader(cp);
    private FileManager fm;

    private VehicleRental vr;

    public RentalControl() {
        fm = new FileManagerBuilder(cp, dr).build();
        try {
            vr = fm.importData();
            cp.printLine("Zaimportowane dane z pliku");
        } catch (DataImportException | InvalidDataException e){
            cp.printLine(e.getMessage());
            cp.printLine("Zainicjowano nową bazę.");
            vr = new VehicleRental();
        }
    }

    void controlLoop(){
        Option option;

        do{
            printOptions();
            option = getOption();
            switch (option){
                case EXIT:
                    exit();
                    break;
                case ADD:
                    add();
                    break;
                case SHOW:
                    show();
                    break;
                case DELETE:
                    delete();
                    break;
                case ADD_PRIVATE_CLIENT:
                    addPrivateClient();
                    break;
                case PRINT_PRIVATE_CLIENTS:
                    printPrivateClients();
                    break;
                case FIND_VEHICLE:
                    findVehicle();
                    break;
                default:
                    cp.printLine("Wybierz poprawną opcję! Spróbuj ponownie!");
            }
        } while (option != Option.EXIT);
    }

    private void add(){
        AddOptions ao = new AddOptions(vr, cp, dr);
        ao.addLoop();
    }

    private void show(){
        ShowOptions so = new ShowOptions(vr, cp, dr);
        so.showLoop();
    }

    private void delete(){
        DeleteOptions delete = new DeleteOptions(vr, cp, dr);
        delete.deleteLoop();
    }

    private void findVehicle(){
        cp.printLine("Podaj numer vin:");
        String vin = dr.getString();
        String notFoundMessage = "Brak pojazdów z takim numerem vin w wypożyczalni";
        vr.findByVin(vin)
                .map(Vehicles::toString)
                .ifPresentOrElse(System.out::println, ()-> System.out.println(notFoundMessage));
    }

    private void addPrivateClient(){
        PrivateClient pc = dr.createPrivateClient();
        try{
            vr.addClient(pc);
        } catch (ClientAlreadyExistsException e){
            cp.printLine(e.getMessage());
        }
    }

    private void printPrivateClients(){
        cp.printPrivateClients(vr.getSortedUsers(
                Comparator.comparing(PrivateClient::getPesel, String.CASE_INSENSITIVE_ORDER)));
    }

    private Option getOption(){
        boolean optionOk = false;
        Option option = null;
        while (!optionOk){
            try{
                option = Option.numberToOption(dr.getInt());
                optionOk = true;
            } catch (NoSuchOptionException ex){
                cp.printLine(ex.getMessage() + ", podaj ponownie:");
            } catch (InputMismatchException ignored){
                cp.printLine("Wprowadzono wartość, która nie jest liczbą, podaj ponownie:");
            }
        }
        return option;
    }

    private void printOptions(){
        cp.printLine("Wybierz jedną z poniższych opcji: ");
        for(Option o: Option.values()){
            cp.printLine(o.toString());
        }
    }

    private void exit(){
        try {
            fm.exportData(vr);
            cp.printLine("Export danych do pliku zakończony powodzeniem");
        } catch (DataExportException e){
            cp.printLine(e.getMessage());
        }
        cp.printLine("Do zobaczenia! ");
        dr.close();
    }

    private enum Option {

        EXIT(0, "Wyjście z programu"),
        ADD(1, "Dodanie kolejnego pojazdu do wypożyczalni"),
        SHOW(2, "Wyświetlenie pojazdów dostępnych w wypożyczalni"),
        DELETE(3, "Usuń pojazd z wypożyczalni"),
        ADD_PRIVATE_CLIENT(4, "Dodaj prywatnego klienta"),
        PRINT_PRIVATE_CLIENTS(5, "Wyświetl prywatnych klientów"),
        FIND_VEHICLE(6,"Wyszukaj pojazd");

        private int value;
        private String description;

        Option(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public String toString() {
            return value + " - " + description;
        }

        static Option numberToOption(int number) throws NoSuchOptionException {
            try {
                return Option.values()[number];
            } catch (ArrayIndexOutOfBoundsException ex){
                throw new NoSuchOptionException("Nie ma takiej opcji w programie: " + number);
            }
        }
    }
}