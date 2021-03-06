package main.window.main.sub.ride;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.window.main.MainWindowController;
import main.window.main.sub.rating.newRatingController;
import main.window.main.sub.rating.reviewsWindowController;
import transpool.logic.traffic.item.Ride;
import transpool.logic.traffic.item.RideForTremp;
import transpool.logic.traffic.item.SubRide;
import transpool.logic.user.Driver;
import transpool.logic.user.User;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RideSubWindowController {

    private MainWindowController mainController;
    private Map<Integer, Ride> ridesVisibleInView;
    private Map<Integer, RideForTremp> trempsVisibleInView;

    private boolean showingTremps = false;

    @FXML
    private Label ridePriceTextField;

    @FXML
    private Label userValue;

    @FXML
    private Label departValue;

    @FXML
    private Label arriveValue;

    @FXML
    private Label ppkValue;

    @FXML
    private Label fuelValue;

    @FXML
    private Label rideDuration;

    @FXML
    private Label rideRankValue;

    @FXML
    private Label StartingdayValue;

    @FXML
    private Label ReputabelValue;

    @FXML
    private Label mainTitle;

    @FXML
    private ListView<String> ridesListView;

    @FXML
    private Button addNewRideBtn;

    @FXML
    private Button assignTrempBtn;

    @FXML
    private Button cancelTrempsBtn;

    @FXML
    private Button reviewsBtn;

    @FXML
    void onClickReviewBtn(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        stage.setResizable(false);
        URL resource = getClass().getResource("/main/window/main/sub/rating/newReviewWindow.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(resource);
        Parent root = loader.load();
        reviewsWindowController controller = loader.getController();

        int index = this.ridesListView.getSelectionModel().getSelectedIndex();
        Ride selectedRide = this.ridesVisibleInView.get(index);

        if(index != -1)
            controller.setReviewTextAreaValue(selectedRide.getRideOwner().getRanks());

        Scene scene = new Scene(root, 356, 363);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void onClickAssignTrempBtn(ActionEvent event) {
        this.mainController.assignSelectedTrempRequestToRide();
        updateAfterDoneWithTremps();
    }

    public RideForTremp getSelectedTremp(){
        int index = this.ridesListView.getSelectionModel().getSelectedIndex();
        return this.trempsVisibleInView.get(index);
    }

    @FXML
    void onClickCancelTrempBtn(ActionEvent event) {
        updateAfterDoneWithTremps();
    }

    private void updateAfterDoneWithTremps(){
        this.showingTremps = false;
        updateTrempButtons();
        updateMainTitle();
        updateNewButton();
        this.mainController.refreshTrempState();
//        updateRidesList();
    }

    public void showTremps(List<RideForTremp> tremps){
        this.showingTremps = true;
        updateMainTitle();
        updateTrempsList(tremps);
        updateNewButton();
    }

    @FXML
    void onAddNewRideBtnClick(ActionEvent event) throws IOException {
        this.mainController.createNewRide();

    }
    public void setMainController(MainWindowController mainController) {
        this.mainController = mainController;
    }

    public void updateRidesList(){
        this.addNewRideBtn.setVisible(true);

        updateAfterDoneWithTremps();
        this.ridesVisibleInView = new HashMap<>();
        int index = 0;
        this.ridesListView.getItems().clear();
        List<Ride> allRides = this.mainController.getAllRides();
        for(Ride ride : allRides) {
            String toSHow = String.format("%d - %s",
                    ride.getID(),
                    ride.getRideOwner().getUser().getName());
            this.ridesListView.getItems().add(toSHow);
            ridesVisibleInView.put(index++, ride);
        }
    }



    public void updateTrempsList(List<RideForTremp> tremps){
        this.trempsVisibleInView = new HashMap<>();
        int index = 0;
        this.ridesListView.getItems().clear();

        for(RideForTremp ride : tremps) {
            String toSHow = String.format("%d - %s",
                    ride.getID(),
                    String.join(",", ride.getDrivers()));
            this.ridesListView.getItems().add(toSHow);
            trempsVisibleInView.put(index++, ride);
        }
    }

    @FXML
    void onRideSelected(MouseEvent event) {
        if (this.mainController != null){
            updateTrempButtons();

            if (this.showingTremps)
                updateTrempInfo();
            else
                updateRideInfo();

        }
    }

    private void updateTrempInfo() {

        assignTrempBtn.setVisible(true);
        cancelTrempsBtn.setVisible(true);

        int index = this.ridesListView.getSelectionModel().getSelectedIndex();
        RideForTremp selectedRide = this.trempsVisibleInView.get(index);

        updateTrempTextFields(selectedRide);
        this.mainController.showRideForTremp(selectedRide);
    }

    private void updateTrempTextFields(RideForTremp selectedRide) {
        String owners = selectedRide.getSubRides().stream().map(SubRide::getOriginalRide)
                .map(Ride::getRideOwner)
                .map(Driver::getUser)
                .map(User::getName).collect(Collectors.joining(","));
        userValue.setText(owners);

        String departTime = selectedRide.getSubRides().get(0).getSchedule().getStartTime().toString();
        departValue.setText(departTime);

        String arriveTime = selectedRide.getSubRides().get(selectedRide.getSubRides().size() - 1).getSchedule().getEndTime().toString();
        arriveValue.setText(arriveTime);

        String ppks = selectedRide.getSubRides().stream().map(SubRide::getOriginalRide).map(Ride::getPricePerKilometer).map(Object::toString).collect(Collectors.joining(","));
        ppkValue.setText(ppks);

        double avgFuel = selectedRide.getAverageFuelUsage();
        fuelValue.setText(String.format("%.2f", avgFuel));

        long duration = Duration.between(selectedRide.getSchedule().getEndDateTime(),
                selectedRide.getSchedule().getStartDateTime()).abs().toMinutes();
        rideDuration.setText(String.valueOf(duration));

        double averageRank = selectedRide.getAverageRank();
        rideRankValue.setText(String.format("%.2f" ,averageRank));
        ridePriceTextField.setText(String.format("Price: %.2f", selectedRide.getTotalCost()));
        ridePriceTextField.setVisible(true);
    }

    private void updateRideInfo() {

        assignTrempBtn.setVisible(false);
        cancelTrempsBtn.setVisible(false);
        this.reviewsBtn.setVisible(true);

        int index = this.ridesListView.getSelectionModel().getSelectedIndex();
        Ride selectedRide = this.ridesVisibleInView.get(index);
        initRideLabels(selectedRide);

        this.mainController.switchLiveMapOff();
        if (selectedRide != null) {
            this.mainController.updateMapRoadsByRides(new LinkedList<Ride>() {{
                add(selectedRide);
            }});
            this.mainController.clearMapStationsData();
            this.mainController.updateMapStationsWithPartsOfRides(selectedRide.getPartsOfRide(), null);
        }

        this.mainController.updateMatchBtn();
    }

    private void updateTrempButtons() {
        updateNewButton();
        if(this.showingTremps){
            this.assignTrempBtn.setVisible(true);
            this.cancelTrempsBtn.setVisible(true);
        } else{
            this.assignTrempBtn.setVisible(false);
            this.cancelTrempsBtn.setVisible(false);
        }
    }

    private void updateMainTitle(){
        if(this.showingTremps){
            this.mainTitle.setText("Choose A Tremp");
        } else
            this.mainTitle.setText("Rides");
    }
    private void updateNewButton() {
        if(this.showingTremps){
           this.addNewRideBtn.setDisable(true);
        } else
            this.addNewRideBtn.setDisable(false);

    }

    private void initRideLabels(Ride ride) {
        userValue.setText(ride.getRideOwner().getUser().getName());
        departValue.setText(String.valueOf(ride.getSchedule().getStartTime()));
        arriveValue.setText(String.valueOf(ride.getSchedule().getEndTime()));
        ppkValue.setText(String.valueOf(ride.getPricePerKilometer()));
        fuelValue.setText(String.format("%.2f", ride.getAverageFuelUsage()));
        rideDuration.setText(String.valueOf(ride.getTotalTimeOfRide()));
        rideRankValue.setText("");
        rideRankValue.setText(String.format("%.2f(%d)" ,ride.getRideOwner().getAverageScore(),ride.getRideOwner().getRankNum()));
        StartingdayValue.setText(String.valueOf(ride.getSchedule().getStartDay()));
        ReputabelValue.setText(String.valueOf(ride.getSchedule().getRepeatType()));
        ridePriceTextField.setText("");
        ridePriceTextField.setVisible(false);
    }

    public void clear(){
        this.ridesListView.getItems().clear();
    }

    public void clearSelection() {
        this.ridesListView.getSelectionModel().clearSelection();
        userValue.setText("");
        departValue.setText("");
        arriveValue.setText("");
        ppkValue.setText("");
        fuelValue.setText("");
        rideDuration.setText("");
        rideRankValue.setText("");
        StartingdayValue.setText("");
        ReputabelValue.setText("");
        ridePriceTextField.setText("");
        ridePriceTextField.setVisible(false);
        reviewsBtn.setVisible(false);
    }

    public void showNoTrempsAvailableTitle() {
        this.mainTitle.setText("Sorry.... no matches for your request");
    }

    public void showRidesTitle() {
        this.mainTitle.setText("Rides");
    }

    @FXML
    public void initialize() {
        this.assignTrempBtn.setVisible(false);
        this.cancelTrempsBtn.setVisible(false);
        this.addNewRideBtn.setVisible(false);
        this.reviewsBtn.setVisible(false);
    }
}
