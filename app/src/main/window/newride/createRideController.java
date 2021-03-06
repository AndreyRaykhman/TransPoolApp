package main.window.newride;

import com.sun.javaws.exceptions.CacheAccessException;
import enums.RepeatType;
import exception.CapacityException;
import exception.NoRoadBetweenStationsException;
import exception.PricePerKilometerException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import main.window.main.MainWindowController;
import transpool.logic.handler.LogicHandler;
import transpool.logic.map.structure.Road;
import transpool.logic.map.structure.Station;
import transpool.logic.traffic.item.Ride;
import transpool.logic.user.User;
import transpool.ui.request.type.NewRideRequest;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class createRideController {

    private Stage stage;
    private MainWindowController mainController;
    private LogicHandler logicHandler;
    private NewRideRequest request;
    private ObservableList stationsNames;
    private List<String> stationsNamesWithRoad;
    private List<String> path;
    private String currentStation;
    private SimpleStringProperty pathProperty;


    @FXML
    private Button createBtn;

    @FXML
    private Button cancelBtn;


    @FXML
    private Label Reputabel;

    @FXML
    private TextField userNameTextField;

    @FXML
    private ChoiceBox<String> addStationChoiceBox;

    @FXML
    private ChoiceBox<String> removeStationChoiceBox;

    @FXML
    private Spinner<Integer> daySpinner;

    @FXML
    private ChoiceBox<Integer> hourChoiceBox;

    @FXML
    private ChoiceBox<Integer> minutesChoiceBox;

    @FXML
    private TextField ppkTextField;

    @FXML
    private TextField capacityTextField;

    @FXML
    private ChoiceBox<RepeatType> reputabelChoiceBox;

    @FXML
    private Label pathLabel;

    @FXML
    private Label exceptionLabel;

    @FXML
    private Button addNewRideBtn;

    @FXML
    private Label CapacityLabel;

    @FXML
    private Label PPKLabel;

    @FXML
    void onAddNewRideBtnClick(ActionEvent event) throws IOException {
        this.mainController.createNewRide();
    }

    @FXML
    void onClickCancelButton(ActionEvent event) {
        stage.close();
    }

    @FXML
    void onClickCreateBtn(ActionEvent event) throws NoRoadBetweenStationsException {
        exceptionLabel.setText("");
        CapacityLabel.setTextFill(Color.WHITE);
        PPKLabel.setTextFill(Color.WHITE);

        try{
            initNewRideRequest();
            logicHandler.addRide(createNewRideFromRequest(request));
            this.mainController.updateRidesList();
            this.stage.close();
        }catch(NumberFormatException e){

        } catch (PricePerKilometerException e) {
            exceptionLabel.setText(e.getMgs());
            PPKLabel.setTextFill(Color.RED);
        } catch (CapacityException e) {
            exceptionLabel.setText(e.getMgs());
            CapacityLabel.setTextFill(Color.RED);
        }
    }

    private void initNewRideRequest() throws NumberFormatException, PricePerKilometerException, CapacityException {

        setCarCapacity();
        setPricePerKilometer();
        request.setStations(path);
        request.setUserName(userNameTextField.getText());
        request.setRepeatType(reputabelChoiceBox.getValue());
        request.setStartTime(LocalTime.of(hourChoiceBox.getValue(), minutesChoiceBox.getValue()));
        request.setDay(daySpinner.getValue());
    }

    private void setPricePerKilometer() throws PricePerKilometerException {
        try{
            if(Integer.parseInt(ppkTextField.getText()) < 0) {
                throw new PricePerKilometerException("Please Enter PPK > 0");
            }

        } catch (NumberFormatException e){
            throw new PricePerKilometerException("Please Enter PPK as Numbers only");
        }
        request.setPricePerKilometer(Integer.parseInt(ppkTextField.getText()));
    }

    private void setCarCapacity() throws CapacityException {
        try{
            if(Integer.parseInt(capacityTextField.getText()) < 0) {
                throw new CapacityException("Please Enter Car Capacity > 0");
            }

        } catch (NumberFormatException e){
            throw new CapacityException("Please Enter Car Capacity as Numbers only");
        }

        request.setCarCapacity(Integer.parseInt(capacityTextField.getText()));
    }


    @FXML
    void onFromStationSelected(MouseEvent event) {

    }

    @FXML
    void onToStationSelected(MouseEvent event) {

    }

    private Ride createNewRideFromRequest(NewRideRequest request) throws NoRoadBetweenStationsException {
        List<Road> roads = logicHandler.getRoadsFromStationsNames(request.getStations());
        User user = logicHandler.getUserByName(request.getUserName());
        Ride newRide = logicHandler.createNewEmptyRide(user, roads, request.getCarCapacity());

        newRide.setSchedule(request.getStartTime(), request.getDay(), request.getRepeatType());
        newRide.setPricePerKilometer(request.getPricePerKilometer());

        return newRide;
    }

    public void addStationChoiceBoxListener(){
        // this is that when you choose a station the choicebox will delete all the stations without a road

        this.addStationChoiceBox.getSelectionModel().selectedItemProperty().addListener((v, oldStation, newStation) ->
        {

            if (newStation != null) {
                //set Path label
                path.add(newStation);
                this.removeStationChoiceBox.getItems().add(newStation);
                pathProperty.set(String.join("->", path));

                //get all the stations with road from newStation
                updateAddStationChoiceBoxAccordingToStationName(newStation);

                this.addStationChoiceBox.getItems().clear();
                this.addStationChoiceBox.getItems().addAll(stationsNamesWithRoad);
            }
        });

        this.removeStationChoiceBox.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) ->
        {
            path.remove(newValue);
            pathProperty.set(String.join("->", path));
            this.removeStationChoiceBox.getItems().remove(newValue);
            String lastStation = path.size() > 0 ? path.get(0) : null;

            if (lastStation != null)
                updateAddStationChoiceBoxAccordingToStationName(lastStation);
            else
                addAllAvailableStationsToAddChoiceBox();
        });

    }

    private void updateAddStationChoiceBoxAccordingToStationName(String fromStationName){
        stationsNamesWithRoad = logicHandler.getStationFromName(fromStationName).getStationsAccessedFromCurrentStation()
                .stream()
                .map(Station::getName)
                .filter(stationName -> !this.path.contains(stationName))
                .collect(Collectors.toList());
    }

    public createRideController(){
        request = new NewRideRequest();
        addStationChoiceBox = new ChoiceBox<>();
        removeStationChoiceBox = new ChoiceBox<>();
        stationsNames = FXCollections.observableArrayList();
        stationsNamesWithRoad = new ArrayList<>();
        pathProperty = new SimpleStringProperty();
        path = new ArrayList<>();
    }

    @FXML
    public void initialize() {
        Platform.runLater(this::initStations);

        pathLabel.textProperty().bind(pathProperty);

        initdaySpinner();

        initHourAndMin();

        initRepeat();
    }

    private void initRepeat() {

        reputabelChoiceBox.getItems().add(RepeatType.ONE_TIME);
        reputabelChoiceBox.getItems().add(RepeatType.DAILY);
        reputabelChoiceBox.getItems().add(RepeatType.BIDAIILY);
        reputabelChoiceBox.getItems().add(RepeatType.WEEKLY);
        reputabelChoiceBox.getItems().add(RepeatType.MONTHLY);

    }

    private void initHourAndMin() {
        ObservableList min = FXCollections.observableArrayList();
        ObservableList hour = FXCollections.observableArrayList();

        for(int i=5; i<=60; i+=5) {min.add(i);}
        for(int i=1; i<=24; i++) {hour.add(i);}

        hourChoiceBox.getItems().addAll(hour);
        minutesChoiceBox.getItems().addAll(min);
    }

    private void initdaySpinner() {
        SpinnerValueFactory<Integer> spinerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,100,1);
        this.daySpinner.setValueFactory(spinerFactory);
    }

    private void initStations() {

        addAllAvailableStationsToAddChoiceBox();

        addStationChoiceBoxListener();
    }

    private void addAllAvailableStationsToAddChoiceBox() {
        List<String> allStationsNames = logicHandler.getAllStations().stream().map(Station::getName).collect(Collectors.toList());
        this.addStationChoiceBox.getItems().addAll(allStationsNames);
    }

    public void setLogicHandler(LogicHandler logicHandler) { this.logicHandler = logicHandler;  }

    public void setStage(Stage stage) { this.stage = stage;  }

    public void setMainController(MainWindowController mainController) { this.mainController = mainController;  }

}
