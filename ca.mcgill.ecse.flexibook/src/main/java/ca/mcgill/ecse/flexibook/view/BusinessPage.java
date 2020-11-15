package ca.mcgill.ecse.flexibook.view;


import ca.mcgill.ecse.flexibook.controller.BusinessController;
import ca.mcgill.ecse.flexibook.controller.InvalidInputException;
import ca.mcgill.ecse.flexibook.controller.TOBusiness;
import ca.mcgill.ecse.flexibook.controller.TOBusinessHour;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BusinessPage extends JFrame {

    // private static final long serialVersionUID = ;

    private String error = "";
    private JLabel errorMessage;

    // Components for owner view
    private final JTextField bName = new JTextField();
    private final JTextField bAddress = new JTextField();
    private final JTextField bPhoneNumber = new JTextField();
    private final JTextField bEmail = new JTextField();
    private final JButton createService = new JButton();
    private final JButton createCombo = new JButton();

    // Components for customer view
    private final JLabel name = new JLabel();
    private final JLabel address = new JLabel();
    private final JLabel phoneNumber = new JLabel();
    private final JLabel email = new JLabel();
    private final JButton createAppointment = new JButton();
    private final JButton viewCalendar = new JButton();

    // Static components
    private final JLabel flexibookIcon = new JLabel(new ImageIcon("src/main/java/ca/mcgill/ecse/flexibook/view/images/FlexibookIcon.png"));
    private final JLabel accountIcon = new JLabel(new ImageIcon("src/main/java/ca/mcgill/ecse/flexibook/view/images/AccountIcon.png"));
    private final JLabel addressIcon = new JLabel(new ImageIcon("src/main/java/ca/mcgill/ecse/flexibook/view/images/AddressIcon.png"));
    private final JLabel emailIcon = new JLabel(new ImageIcon("src/main/java/ca/mcgill/ecse/flexibook/view/images/EmailIcon.png"));
    private final JLabel phoneNumberIcon = new JLabel(new ImageIcon("src/main/java/ca/mcgill/ecse/flexibook/view/images/PhoneNumberIcon.png"));

    private static final JLabel title = new JLabel("Flexibook - Business Information");
    private static final JLabel generalInfo = new JLabel("General Information");
    private static final JLabel services = new JLabel("Services");
    private static final JLabel combos = new JLabel("Combos");
    private static final JLabel businessHours = new JLabel("Business Hours");

    private static final Dimension infoSuggestedDimension = new Dimension(250, 34);
    private static final Dimension infoMaxDimension = new Dimension(250, 34);

    // Constructor
    public BusinessPage(boolean isOwner) {
        initComponents(isOwner);
        refreshData();
    }

    // Initialize components
    private void initComponents(boolean isOwner) {

        // Set text
        Font titleText = new Font("SansSerif", Font.PLAIN, 28);
        Font subTitleText = new Font("SansSerif", Font.PLAIN, 18);
        Font regularText = new Font("SansSerif", Font.PLAIN, 14);

        title.setFont(subTitleText);
        services.setFont(subTitleText);
        combos.setFont(subTitleText);
        generalInfo.setFont(subTitleText);
        businessHours.setFont(subTitleText);

        errorMessage = new JLabel();
        errorMessage.setFont(regularText);
        errorMessage.setForeground(Color.red);

        // Get business info
//        TOBusiness businessInfo = BusinessController.getBusinessInfo();
//        String nameText = (businessInfo.getName() == null) ? "" : businessInfo.getName();
//        String addressText = (businessInfo.getAddress() == null) ? "" : businessInfo.getAddress();
//        String phoneNumberText = (businessInfo.getPhoneNumber() == null) ? "" : businessInfo.getPhoneNumber();
//        String emailText = (businessInfo.getEmail() == null) ? "" : businessInfo.getEmail();

        // TODO: remove when business is set up
        String nameText = "A Business Name";
        String addressText = "123 Somewhere St.";
        String phoneNumberText = "123-456-7890";
        String emailText = "someone@mail.com";

//        ArrayList<TOBusinessHour> businessHoursList = BusinessController.getBusinessHours();

        // TODO: remove when business is set up
        ArrayList<TOBusinessHour> businessHoursList = new ArrayList<>();
        businessHoursList.add(new TOBusinessHour("Monday", "09:00", "05:00"));
        businessHoursList.add(new TOBusinessHour("Wednesday", "12:00", "05:00"));
        businessHoursList.add(new TOBusinessHour("Thursday", "15:00", "10:00"));
        businessHoursList.add(new TOBusinessHour("Saturday", "06:00", "14:30"));

        if (isOwner) {
            // set text
            bName.setText(nameText);
            bName.setPreferredSize(infoSuggestedDimension);
            bName.setMaximumSize(infoMaxDimension);
            bAddress.setText(addressText);
            bAddress.setPreferredSize(infoSuggestedDimension);
            bAddress.setMaximumSize(infoMaxDimension);
            bPhoneNumber.setText(phoneNumberText);
            bPhoneNumber.setPreferredSize(infoSuggestedDimension);
            bPhoneNumber.setMaximumSize(infoMaxDimension);
            bEmail.setText(emailText);
            bEmail.setPreferredSize(infoSuggestedDimension);
            bEmail.setMaximumSize(infoMaxDimension);
        } else {
            name.setText(nameText);
            name.setFont(titleText);
            name.setPreferredSize(infoSuggestedDimension);
            address.setText(addressText);
            address.setFont(regularText);
            address.setPreferredSize(infoSuggestedDimension);
            phoneNumber.setText(phoneNumberText);
            phoneNumber.setFont(regularText);
            phoneNumber.setPreferredSize(infoSuggestedDimension);
            email.setText(emailText);
            email.setFont(regularText);
            email.setPreferredSize(infoSuggestedDimension);
        }

        // Window settings
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Flexibook");

        // Event listeners


        // Layout

        JPanel businessPanel = new JPanel();

        // outer container (to center all content)
        Container container = getContentPane();
        container.setLayout(new GridBagLayout());
        container.setBackground(Color.white);
        add(businessPanel);

        // inner container
        GroupLayout layout = new GroupLayout(businessPanel);

        businessPanel.setLayout(layout);
        businessPanel.setBackground(Color.white);

        // Gaps between elements
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Add items to menu
        ArrayList<String> listOfStrings = new ArrayList<>();
        listOfStrings.add("thing");
        listOfStrings.add("thing2");
        listOfStrings.add("thing3");

        // add services
        GroupLayout.ParallelGroup pMenuGroup = layout.createParallelGroup().addComponent(services);
        GroupLayout.SequentialGroup sMenuGroup = layout.createSequentialGroup().addComponent(services).addGap(10);

        for (String item : listOfStrings) {
            JLabel menuItem = new JLabel(item);
            menuItem.setFont(regularText);
            pMenuGroup.addComponent(menuItem);
            sMenuGroup.addComponent(menuItem);
        }

        // add combos
        pMenuGroup.addComponent(combos);
        sMenuGroup.addGap(50).addComponent(combos).addGap(10);

        for (String item : listOfStrings) {
            JLabel menuItem = new JLabel(item);
            menuItem.setFont(regularText);
            pMenuGroup.addComponent(menuItem);
            sMenuGroup.addComponent(menuItem);
        }

        // Add business hours
        GroupLayout.ParallelGroup pHours = layout.createParallelGroup().addComponent(businessHours);
        GroupLayout.SequentialGroup sHours = layout.createSequentialGroup().addComponent(businessHours).addGap(25);
        GroupLayout.ParallelGroup pOuter = layout.createParallelGroup();
        GroupLayout.SequentialGroup sOuter = layout.createSequentialGroup();

        // Generate lists of business hours
        if (isOwner) {
            for (TOBusinessHour hour : businessHoursList) {
                GroupLayout.ParallelGroup pInner = layout.createParallelGroup();
                GroupLayout.SequentialGroup sInner = layout.createSequentialGroup();
                JTextField day = new JTextField(hour.getDayOfWeek());
                day.setFont(regularText);
                pInner.addComponent(day);
                sInner.addComponent(day);
                JTextField startTime = new JTextField(hour.getStartTime());
                startTime.setFont(regularText);
                pInner.addComponent(startTime);
                sInner.addComponent(startTime);
                JTextField endTime = new JTextField(hour.getEndTime());
                endTime.setFont(regularText);
                pInner.addComponent(endTime);
                sInner.addComponent(endTime);
                sOuter.addGroup(pInner).addGap(10);
                pOuter.addGroup(sInner);
            }
            pHours.addGroup(sOuter);
            sHours.addGroup(pOuter);
        } else {
            for (TOBusinessHour hour : businessHoursList) {
                GroupLayout.ParallelGroup pInner = layout.createParallelGroup();
                GroupLayout.SequentialGroup sInner = layout.createSequentialGroup();
                JLabel day = new JLabel(hour.getDayOfWeek() + ": ");
                day.setMinimumSize(new Dimension(150, 20));
                pInner.addComponent(day);
                sInner.addComponent(day);
                day.setFont(regularText);
                JLabel time = new JLabel(hour.getStartTime() + "-" + hour.getEndTime());
                time.setFont(regularText);
                pInner.addComponent(time);
                sInner.addComponent(time);
                pHours.addGroup(sInner);
                sHours.addGroup(pInner);
            }
        }


        // Horizontal layout
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(flexibookIcon).addGap(20).addComponent(isOwner ? bName : name).addGap(400).addComponent(accountIcon))
                        .addGroup(layout.createParallelGroup().addComponent(title))
                        .addGroup(layout.createParallelGroup().addComponent(errorMessage))
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(pMenuGroup).addGap(150)
                                .addGroup(layout.createParallelGroup().addComponent(generalInfo)
                                        .addGroup(layout.createSequentialGroup().addComponent(addressIcon).addGap(20).addComponent(isOwner ? bAddress : address))
                                        .addGroup(layout.createSequentialGroup().addComponent(phoneNumberIcon).addGap(20).addComponent(isOwner ? bPhoneNumber : phoneNumber))
                                        .addGroup(layout.createSequentialGroup().addComponent(emailIcon).addGap(20).addComponent(isOwner ? bEmail : email))
                                        .addGroup(pHours) ))
        );

        // Vertical layout
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup().addComponent(flexibookIcon).addComponent(isOwner ? bName : name).addComponent(accountIcon)).addGap(50)
                        .addGroup(layout.createSequentialGroup().addComponent(title)).addGap(25)
                        .addGroup(layout.createSequentialGroup().addComponent(errorMessage)).addGap(25)
                        .addGroup(layout.createParallelGroup()
                                .addGroup(sMenuGroup)
                                .addGroup(layout.createSequentialGroup().addComponent(generalInfo).addGap(25)
                                        .addGroup(layout.createParallelGroup().addComponent(addressIcon).addGap(20).addComponent(isOwner ? bAddress : address))
                                        .addGroup(layout.createParallelGroup().addComponent(phoneNumberIcon).addGap(20).addComponent(isOwner ? bPhoneNumber : phoneNumber))
                                        .addGroup(layout.createParallelGroup().addComponent(emailIcon).addGap(20).addComponent(isOwner ? bEmail : email)).addGap(50)
                                        .addGroup(sHours) ))
        );

        // Set window size
//        pack(); // automatically adjust for content
        setExtendedState(JFrame.MAXIMIZED_BOTH); // full window

    }

    // Refresh components
    private void refreshData() {

        errorMessage.setText(error);

    }

}

