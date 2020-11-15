  
package ca.mcgill.ecse.flexibook.controller;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.model.*;
import ca.mcgill.ecse.flexibook.controller.AppointmentController;
import ca.mcgill.ecse.flexibook.persistence.FlexiBookPersistence;

import java.util.ArrayList;
import java.util.List;
import java.sql.Date;
import java.sql.Time;

/**
 * @author Aidan Jackson
 *
 * Class handles the Creation, Update, and Deletion of the Customer accounts.
 * */
public class AccountManagerController {
    public AccountManagerController(){
    }

    /**Creates a new customer account after checking all corresponding restrictions. Throws an InvalidInputException with the appropriate error message upon
     * discovering an invalid input.
     * @author Aidan Jackson
     * @param username specifies what the client would like their profile username to be.
     * @param password specifies what the client would like their profile password to be.
     * @throws InvalidInputException Is thrown when the provided user inputs do not meet the requirements set by the flexiBook application.
     */
    public static void createCustomer(String username, String password) throws InvalidInputException{
        String errorMessage = "";
        FlexiBook flexiBook = FlexiBookApplication.getFlexibook();

        if(username.equals(""))errorMessage += "The user name cannot be empty";

        if(password.equals("")) errorMessage += "The password cannot be empty";

        if(doesUsernameExist(null,username , flexiBook)) errorMessage += "The username already exists";

        if(FlexiBookApplication.getCurrentUser() != null && flexiBook.getOwner() != null){
            if(FlexiBookApplication.getCurrentUser() == flexiBook.getOwner()){
                errorMessage += "You must log out of the owner account before creating a customer account";
            }
        }

        if(errorMessage.length() > 0){
            throw new InvalidInputException(errorMessage.trim());
        }

        try{
            Customer customer = new Customer(username, password, flexiBook);
            flexiBook.addCustomer(customer);
            FlexiBookPersistence.save(flexiBook);
        }
        catch(RuntimeException e){
            if (e.getMessage().trim().equals("Cannot create due to duplicate name. See http://manual.umple.org?RE003ViolationofUniqueness.html")){
                errorMessage = "Customer " + username + " already exists";
            }
            throw new InvalidInputException(errorMessage.trim());
        }

        //may want to link this creation to the current user and auto log in
    }

    /** Method detects whether the current user is the owner or a customer and allows for the change of password or username and password accordingly.
     * Throws an InvalidInputException with the appropriate error message upon discovering an invalid input.
     * @author Aidan Jackson
     * @param newUsername specifies the Username that the client would like to update their current username to.
     * @param newPassword specifies the Password that the client would like to update their current username to.
     * @throws InvalidInputException Is thrown when the provided user inputs do not meet the requirements set by the flexiBook application.
     */
    public static void updateUserInfo(String newUsername,String newPassword)throws InvalidInputException{
        String errorMessage = "";
        FlexiBook flexiBook = FlexiBookApplication.getFlexibook();
        User activeUser = FlexiBookApplication.getCurrentUser();
        System.out.println(activeUser);
        if(!activeUser.getUsername().equals(flexiBook.getOwner().getUsername())){
            String oldUsername = activeUser.getUsername();
            Customer loggedInCustomer = findCustomer(oldUsername, flexiBook);

            if(newUsername.equals(""))errorMessage += "The user name cannot be empty";

            if(newPassword.equals("")) errorMessage += "The password cannot be empty";

            if(doesUsernameExist(loggedInCustomer.getUsername(),newUsername , flexiBook)) errorMessage += "Username not available";

            if(errorMessage.length() > 0) throw new InvalidInputException(errorMessage.trim());

            loggedInCustomer.setUsername(newUsername);
            loggedInCustomer.setPassword(newPassword);
          
        }
        else{
            Owner loggedInOwner = flexiBook.getOwner();

            if(!newUsername.equals(flexiBook.getOwner().getUsername())) errorMessage += "Changing username of owner is not allowed";

            if(newPassword.equals("")) errorMessage += "The password cannot be empty";

            if(errorMessage.length() > 0) throw new InvalidInputException(errorMessage.trim());

            loggedInOwner.setPassword(newPassword);
        }
      try{
            FlexiBookPersistence.save(flexiBook);
          } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
          }

    }

    /** Removes, deletes, and logs out existing customer accounts from within the application.
     * @author Aidan Jackson
     * @param username specifies the username of the currently logged in user.
     * @param target specifies the username of the targeted account to be delete.
     * @throws InvalidInputException Is thrown when the provided user inputs do not meet the requirements set by the flexiBook application.
     */
    public static void deleteAccount(String username, String target)throws InvalidInputException{
        String errorMessage="";
        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        Customer accountToDelete = findCustomer(target, flexibook);

        if(target.equals(flexibook.getOwner().getUsername()) || !username.equals(target)) errorMessage += "You do not have permission to delete this account";

        if(errorMessage.length() > 0) throw new InvalidInputException(errorMessage.trim());

        deleteUserAppointments(target);
        accountToDelete.delete();
        FlexiBookApplication.setCurrentUser(null);
        try{
            FlexiBookPersistence.save(flexibook);
          } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
          }
    }

    /** Searches the applications list of Customer accounts for the customer with the given username.
     * @author Aidan Jackson
     * @param username specifies the username of the customer that is to be searched for.
     * @param flexiBook specifies the flexiBook that is associated with the provided username.
     * @return Customer Account
     */
    public static Customer findCustomer(String username, FlexiBook flexiBook){
        for(Customer curCustomer:flexiBook.getCustomers()){
            if(curCustomer.getUsername().equals(username)) return curCustomer;
        }
        return null;
    }

    /**Detects whether or not a Customer account possesses the username a customer is trying to switch to.
     * @author Aidan Jackson
     * @param oldUsername specifies the username belonging to the current username.
     * @param newUsername specifies the new username that the client is trying to switch to.
     * @param flexiBook specifies the flexiBook which belongs to the current user.
     * @return True or False
     */
    public static boolean doesUsernameExist(String oldUsername,String newUsername, FlexiBook flexiBook){
        for(Customer aCustomer: flexiBook.getCustomers()){
            if(aCustomer.getUsername().equals(newUsername) && !aCustomer.getUsername().equals(oldUsername))return true;
        }
        return false;
    }

    /** Parses through the list of Appointments and deletes any belonging to the account with according username.
     * @author Aidan Jackson
     * @param username specifies the username of the account which is to be deleted.
     */
    public static void deleteUserAppointments(String username) throws InvalidInputException {
        FlexiBook flexibook = FlexiBookApplication.getFlexibook();
        List<Appointment> appointmentsToDelete = new ArrayList<>();
        for (Appointment currOldAppointment : FlexiBookApplication.getFlexibook().getAppointments()) {
           if(currOldAppointment.getCustomer().getUsername().equals(username)){
                appointmentsToDelete.add(currOldAppointment);
            }
        }
        for (Appointment curAppointment: appointmentsToDelete){
            flexibook.removeAppointment(curAppointment);
            curAppointment.delete();
        }
      try{
            FlexiBookPersistence.save(flexibook);
          } catch (RuntimeException e) {
            throw new InvalidInputException(e.getMessage());
          }
    }
}
