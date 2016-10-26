/* Java II Project 2
 * Chapter 13 Programming Challenge 5 problem statement:
 * Create an application that works like a shopping cart system for a bookstore. 
 * Your application will use a text file named BookPrices.txt that will contain 
 * the names and prices of various books, formatted in the following fashion:
 * 
 * 		I Did It Your Way, 11.95
 * 		The History of Scotland, 14.50
 * 		Learn Calculus in One Day, 29.95
 * 		Feel the Stress, 18.50
 * 
 * Each line in the file contains the name of a book, followed by a comma, followed by the book's retail price.
 * When your applications begins execution, it should read the contents of the file and store the book titles in a list component.
 * The user should be able to select a title from the list and add it to a shopping cart, which is simply another list component.
 * The application should have buttons or menu items that allow users to remove items from the shopping cart, clear the shopping
 * cart of all selections, and check out. When the user checks out, the application should calculate and display the subtotal of all
 * the books in the shopping cart, the sales tax (which is 6% of the subtotal), and the total.
 * 
 * 
 * 
 * I initially wanted to put the panels into their own classes, but I had trouble passing the books HashMap data to it, so
 * I just kept everything in one class file. Creating and configuring the panels was a bit confusing for me, but I am quite happy with
 * the outcome. The only problem that I didn't get to fix was the book title JList not including the cost of the book. 
 * The problem states that the book titles should be stored in a list component, and I guess I interpreted this too literally.
 * I imagine this would bother a user though. I couldn't think of a quick fix either that didn't involve removing the HashMap.
 * The book titles JList uses the keys in the HashMap, which are the book titles, which were transformed into a set and that set
 * turns into an array for the JList. I probably could have just used a regular ArrayList to store the whole book title and 
 * cost, and change each individual element to the format of "title - $0.00".  Despite having these problems, I liked using the HashMap.
 * 
 * Written by Billy Matthews, October 2016.
 */

package bookstore_shopping;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


@SuppressWarnings("serial")
public class ShoppingCart extends JFrame {
	
	private double subtotal;					// Subtotal of the shopping cart
	private final double salesTax = 0.06;		// Sales tax used in finding the total cost
	private final int height = 500;				// Window minimum height
	private final int width = 450;				// Window minimum width
	private Map<String, Double> books;			// HashMap used for storing the book titles and costs
	
	private JPanel bookTitlePanel;				// Holds the book title JList and addButton
	private JList<String> bookTitleList;		// Holds the book titles
	
	private JPanel bookCartPanel;				// Holds the shopping cart JList, remove button, and clear button
	private JList<String> bookCartList;			// Shopping cart list
	private DefaultListModel<String> listModel; // Used to create an empty JList that items can be added into
	
	private JPanel buttonPanel;					// Bottom JPanel used to hold the checkout and exit buttons
	
	public ShoppingCart() throws IOException {
		subtotal = 0.0;
		// Creates the HashMap, which stores the book title (String) key, and the cost (double) value
		books = new HashMap<String, Double>();  
		
		setTitle("Bookstore Shopping Cart");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setLayout(new BorderLayout());
		
		setMinimumSize(new Dimension(height, width));		
		
		buildBookTitlePanel();  // Creates the book title panel
		buildBookCartPanel();	// Creates the shopping cart panel
		buildButtonPanel();		// Creates the bottom button panel
		
		add(bookTitlePanel, BorderLayout.WEST);
		add(bookCartPanel, BorderLayout.EAST);
		add(buttonPanel, BorderLayout.SOUTH);
		
		pack();
		setVisible(true);
	}	
	
//-----------------------------------------------------------------------------------------------------------
	
	// This method builds the left panel, which contains the book title list and the addButton, which adds titles to the cart
	private void buildBookTitlePanel() throws IOException {
		bookTitlePanel = new JPanel();
		bookTitlePanel.setLayout(new BorderLayout());
		bookTitlePanel.setBorder(BorderFactory.createTitledBorder("Books"));
		
		 // This method opens up the BookPrices text file and populates the books HashMap
		findBookTitleAndCosts();
		// Create a Set for the book titles that will populate the JList
		Set<String> keys = books.keySet();
		// In order to populate the JList, we need to convert the Set of keys to an Array of Strings
		bookTitleList = new JList<String>(keys.toArray(new String[keys.size()]));
		// We allow the user to select multiple books at once
		bookTitleList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		bookTitleList.setFixedCellWidth(width/2);
		bookTitlePanel.add(new JScrollPane(bookTitleList));
		
		// The addButton adds a book title to the cart, and also adds the cost of the book to the sub-total
		JButton addButton = new JButton("Add >>");
		addButton.addActionListener(new AddButtonListener());
		// Finally, we add the addButton to the bottom of the panel
		bookTitlePanel.add(addButton, BorderLayout.SOUTH);
	}
	
//-----------------------------------------------------------------------------------------------------------
	
	// This method opens up the BookPrices.txt file and populates the books HashMap with the values found in the file
	private void findBookTitleAndCosts() throws IOException {
		File file = null;
		Scanner inputFile = null;
		try {
			file = new File("BookPrices.txt");
			inputFile = new Scanner(file);
	
			String line;
			while(inputFile.hasNext()) {
				line = inputFile.nextLine();
				// Split the String where a  ',' is found
				// A possible problem could be that a future book title could contain a comma
				String[] parts = line.split("[,]");
				String bookTitle = parts[0].trim();
				double bookCost = Double.parseDouble(parts[1].trim());
				books.put(bookTitle, bookCost);
			}
			inputFile.close();
		} catch(IOException | NullPointerException | IllegalArgumentException e) {
			System.err.format("Error: %s%n", e);
			System.exit(1);
		}

	}
	
//-----------------------------------------------------------------------------------------------------------
	
	// buildBookCartPanel builds the right panel, which contains the shopping cart list, remove button, and clear cart button
	private void buildBookCartPanel() {
		bookCartPanel = new JPanel();
		bookCartPanel.setLayout(new BorderLayout());
		bookCartPanel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));
		
		// Create an empty List Model for Strings
		listModel = new DefaultListModel<String>();
		bookCartList = new JList<String>(listModel);
		bookCartList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		bookCartList.setFixedCellWidth(width/2);

		
		// The remove button will remove all the highlighted titles from the JList
		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new RemoveButtonListener());
	
		// The clear button will remove all elements from the JList and reset the bill amount
		JButton clearButton = new JButton("Clear Cart");
		clearButton.addActionListener(new ClearButtonListener());
		
		// tempButtonPanels holds removeButton and clearButton and uses GridLayout to put the buttons side by side
		JPanel tempButtonPanel = new JPanel(new GridLayout(1,2));
		tempButtonPanel.add(removeButton);
		tempButtonPanel.add(clearButton);
		
		// Lastly, we add a JScrollPane for the book shopping cart, and add the button panel below
		bookCartPanel.add(new JScrollPane(bookCartList));
		bookCartPanel.add(tempButtonPanel, BorderLayout.SOUTH);
	}
	
//-----------------------------------------------------------------------------------------------------------
	
	// buildButtonPanel builds the bottom panel for the window, which contains the Checkout and Exit buttons
	private void buildButtonPanel() {
		buttonPanel = new JPanel(new GridLayout(1,2));
		
		// The checkoutButton calculates the shopping carts contents, which will
		// find the sales tax and add it to the subtotal to find the total cost
		JButton checkoutButton = new JButton("Checkout");
		checkoutButton.addActionListener(new CheckoutButtonListener());
		buttonPanel.add(checkoutButton);
		
		// The exitButton will close the window and exit the program
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ExitButtonListener());
		buttonPanel.add(exitButton);
	}
	
//-----------------------------------------------------------------------------------------------------------
	
	// AddButtonListener adds selected book titles to the book shopping cart list
	private class AddButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// Store the highlighted book titles in the shopping cart and put them in a List
			List<String> bookTitlesList = bookTitleList.getSelectedValuesList();
			
			// Step through the list and retrieve values from the books HashMap with the keys provided from the bookTitlesList 
			for(int i = 0; i < bookTitlesList.size(); ++i) { 
				try {
					double bookCost = books.get(bookTitlesList.get(i));
					// bookTitle appends the cost of the book to the book title String
					String bookTitle = bookTitlesList.get(i) + " - " + String.format("$%.2f", bookCost);
					// Add the book cost to the shopping cart subtotal
					subtotal += bookCost;
					// Lastly, we add the bookTitle to the listModel (which is what the shopping cart JList uses)
					listModel.addElement(bookTitle);
				}
				catch(NullPointerException | IndexOutOfBoundsException x) {
					System.err.print("Something went wrong!\n" + x.getLocalizedMessage());
					System.exit(1);
				}
			}	
		}
		
	}
	
//-----------------------------------------------------------------------------------------------------------
	
	// RemoveButtonListener removes selected indices from the book shopping cart list 
	private class RemoveButtonListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// Get the selected indices from the book shopping cart JList
			int[] bookIndex = bookCartList.getSelectedIndices();
			
			// In case there are no selected items, we do nothing and return without removing an element
			if(bookIndex.length == 0) return;
			
			// Step through the bookIndex array and remove the selected titles
			for(int i = bookIndex.length; i > 0; --i) {
				try {
					int temp = i - 1;
					// We split the String in half, which will contain the book title and cost
					String[] bookTitleTemp = listModel.get(temp).split("[-]");
					String bookTitle = bookTitleTemp[0].trim();
					// We get the book cost with the key provided (which is the book title) and subtract it from the subtotal
					subtotal -= books.get(bookTitle);
					// Then we remove the index from the ListModel
					listModel.remove(bookIndex[temp]);
				}
				catch (ArrayIndexOutOfBoundsException | NullPointerException x) {
					System.err.print("Something has gone wrong!\n" + x.getLocalizedMessage());
					System.exit(1);
				}
			}	
		}
		
	}
		
//-----------------------------------------------------------------------------------------------------------
	
	// Clears the shopping cart list and resets the subtotal
	private class ClearButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) { 
			// Clear the list and set the subtotal to 0
			listModel.clear();
			subtotal = 0.0;
		}
		
	}
	
//-----------------------------------------------------------------------------------------------------------
	
	// Calculates the total cost and prints it onto the screen
	private class CheckoutButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// We get the shopping cart total and output onto the screen
			double total = salesTax * subtotal + subtotal;
			JOptionPane.showMessageDialog(null, ("Subtotal:        " + String.format("$ %.2f", subtotal)
										       + "\nSales Tax:     " + String.format("$ %.2f", (salesTax * subtotal))
										       + "\nTotal:              " + String.format("$ %.2f", total)), 
												 "Checkout", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	
//-----------------------------------------------------------------------------------------------------------
	
	// Opens up a window asking if the user is sure they want to exit
	private class ExitButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// Make sure the user wants to exit, and close the program if so
			int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
			if(choice == JOptionPane.YES_OPTION) System.exit(0);
		}
		
	}
	
}