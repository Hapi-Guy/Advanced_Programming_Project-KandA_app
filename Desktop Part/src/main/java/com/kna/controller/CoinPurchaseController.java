package com.kna.controller;

import com.kna.model.User;
import com.kna.service.AuthService;
import com.kna.service.CoinService;
import com.kna.util.SessionManager;
import com.kna.util.ToastNotification;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * CoinPurchaseController - Handles coin purchasing
 */
public class CoinPurchaseController {
    
    @FXML private Label currentBalanceLabel;
    @FXML private GridPane packagesGrid;
    @FXML private VBox selectedPackageInfo;
    @FXML private Label selectedPackageNameLabel;
    @FXML private Label selectedCoinsLabel;
    @FXML private Label selectedPriceLabel;
    
    private final CoinService coinService;
    private final AuthService authService;
    private User currentUser;
    private CoinService.CoinPackage selectedPackage;

    public CoinPurchaseController() {
        this.coinService = new CoinService();
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        

        
        currentBalanceLabel.setText(String.valueOf(currentUser.getCoins()));
        
        loadCoinPackages();
    }

    private void loadCoinPackages() {
        CoinService.CoinPackage[] packages = coinService.getPackages();
        
        int col = 0;
        int row = 0;
        
        for (CoinService.CoinPackage pkg : packages) {
            VBox packageCard = createPackageCard(pkg);
            packagesGrid.add(packageCard, col, row);
            
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createPackageCard(CoinService.CoinPackage pkg) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(220);
        card.setPrefHeight(200);
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");
        
        // Package name
        Label nameLabel = new Label(pkg.name);
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Coin amount
        Label coinsLabel = new Label( pkg.coins + " Coins");
        coinsLabel.setStyle("-fx-font-size: 16px; -fx-background-color: #FFF3E0; -fx-text-fill: #E65100; -fx-padding: 8px 15px; -fx-background-radius: 15px;");
        
        // Price
        Label priceLabel = new Label("" + pkg.price);
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        
        // Select button
        Button selectButton = new Button("Select");
        selectButton.setOnAction(e -> selectPackage(pkg));
        
        card.getChildren().addAll(nameLabel, coinsLabel, priceLabel, selectButton);
        
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-background-color: #E3F2FD;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle() + "-fx-background-color: white;"));
        card.setOnMouseClicked(e -> selectPackage(pkg));
        
        return card;
    }

    private void selectPackage(CoinService.CoinPackage pkg) {
        selectedPackage = pkg;
        
        selectedPackageNameLabel.setText(pkg.name);
        selectedCoinsLabel.setText(pkg.coins + " Coins");
        selectedPriceLabel.setText("৳ " + pkg.price + " BDT");
        
        selectedPackageInfo.setVisible(true);
    }

    @FXML
    private void handlePurchase() {

        
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Purchase");
        confirmAlert.setHeaderText("Purchase " + selectedPackage.coins + " Coins?");
        confirmAlert.setContentText("You will be charged ৳" + selectedPackage.price + " BDT.");
        
        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
            return;
        }

    }

    private void simulatePayment() {
        // Create a blank white screen
        Stage paymentStage = new Stage();
        paymentStage.initModality(Modality.APPLICATION_MODAL);
        paymentStage.initStyle(StageStyle.UNDECORATED);
        
        StackPane whiteScreen = new StackPane();
        whiteScreen.setStyle("-fx-background-color: white;");
        
        Label processingLabel = new Label("Processing payment...");
        processingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #757575;");
        
        ProgressIndicator progressIndicator = new ProgressIndicator();
        
        VBox content = new VBox(20, progressIndicator, processingLabel);
        content.setAlignment(Pos.CENTER);
        
        whiteScreen.getChildren().add(content);
        
        Scene scene = new Scene(whiteScreen, 400, 300);
        paymentStage.setScene(scene);
        paymentStage.show();
        
        // Auto close after 2 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> {
            paymentStage.close();
            completePurchase();
        });
        delay.play();
    }

    private void completePurchase() {
        try {
            // Process purchase
            coinService.purchaseCoins(selectedPackage.coins, selectedPackage.price);
            
            // Update UI
            currentUser = SessionManager.getInstance().getCurrentUser();
            currentBalanceLabel.setText(String.valueOf(currentUser.getCoins()));
            
//            // Show success
//            ToastNotification.showSuccess("Purchase successful! You now have " +
//                                        currentUser.getCoins() + " Coins");
            
            // Reset selection
            selectedPackageInfo.setVisible(false);
            selectedPackage = null;
            
            // Refresh user data
            authService.refreshCurrentUser();
            
        } catch (Exception e) {
            ToastNotification.showError("Purchase failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        selectedPackageInfo.setVisible(false);
        selectedPackage = null;
    }
}
