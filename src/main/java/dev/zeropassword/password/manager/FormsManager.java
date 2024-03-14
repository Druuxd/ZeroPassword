package dev.zeropassword.password.manager;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import dev.zeropassword.password.PasswordApplication;

import javax.swing.*;
import java.awt.*;

public class FormsManager {
    private PasswordApplication application;
    private static FormsManager instance;

    public static FormsManager getInstance() {
        if (instance == null) {
            instance = new FormsManager();
        }
        return instance;
    }

    private FormsManager() {

    }

    public void initApplication(PasswordApplication application) {
        this.application = application;
    }

    public void showForm(JComponent form) {
        EventQueue.invokeLater(() -> {
            FlatAnimatedLafChange.showSnapshot();
            application.setContentPane(form);
            application.revalidate();
            application.repaint();
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        });
    }
}
