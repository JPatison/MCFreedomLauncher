import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
/*
 * Created by JFormDesigner on Sun Aug 11 02:01:11 CST 2013
 */



/**
 * @author Energy
 */
public class SettingsPanel extends JPanel {
    public SettingsPanel() {
        initComponents();
    }

    private void buttonChangeLangActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void buttonChangeWDActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = LocaleHelper.getMessages();
        labelLanguage = new JLabel();
        comboBoxLanguage = new JComboBox();
        buttonChangeLang = new JButton();
        labelDirectory = new JLabel();
        labelWorkingDirectory = new JLabel();
        buttonChangeWD = new JButton();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 40, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

        //---- labelLanguage ----
        labelLanguage.setText(bundle.getString("SettingsPanel.labelLanguage.text"));
        add(labelLanguage, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));
        add(comboBoxLanguage, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- buttonChangeLang ----
        buttonChangeLang.setText(bundle.getString("SettingsPanel.buttonChangeLang.text"));
        buttonChangeLang.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonChangeLangActionPerformed(e);
            }
        });
        add(buttonChangeLang, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- labelDirectory ----
        labelDirectory.setText(bundle.getString("SettingsPanel.labelDirectory.text"));
        add(labelDirectory, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- labelWorkingDirectory ----
        labelWorkingDirectory.setText(bundle.getString("SettingsPanel.labelWorkingDirectory.text"));
        add(labelWorkingDirectory, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- buttonChangeWD ----
        buttonChangeWD.setText(bundle.getString("SettingsPanel.buttonChangeWD.text"));
        buttonChangeWD.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonChangeWDActionPerformed(e);
            }
        });
        add(buttonChangeWD, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel labelLanguage;
    private JComboBox comboBoxLanguage;
    private JButton buttonChangeLang;
    private JLabel labelDirectory;
    private JLabel labelWorkingDirectory;
    private JButton buttonChangeWD;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
