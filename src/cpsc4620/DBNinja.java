package cpsc4620;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/*
 * This file is where you will implement the methods needed to support this application.
 * You will write the code to retrieve and save information to the database and use that
 * information to build the various objects required by the applicaiton.
 * 
 * The class has several hard coded static variables used for the connection, you will need to
 * change those to your connection information
 * 
 * This class also has static string variables for pickup, delivery and dine-in. 
 * DO NOT change these constant values.
 * 
 * You can add any helper methods you need, but you must implement all the methods
 * in this class and use them to complete the project.  The autograder will rely on
 * these methods being implemented, so do not delete them or alter their method
 * signatures.
 * 
 * Make sure you properly open and close your DB connections in any method that
 * requires access to the DB.
 * Use the connect_to_db below to open your connection in DBConnector.
 * What is opened must be closed!
 */

/*
 * A utility class to help add and retrieve information from the database
 */

public final class DBNinja {
	private static Connection conn;

	// DO NOT change these variables!
	public final static String pickup = "pickup";
	public final static String delivery = "delivery";
	public final static String dine_in = "dinein";

	public final static String size_s = "Small";
	public final static String size_m = "Medium";
	public final static String size_l = "Large";
	public final static String size_xl = "XLarge";

	public final static String crust_thin = "Thin";
	public final static String crust_orig = "Original";
	public final static String crust_pan = "Pan";
	public final static String crust_gf = "Gluten-Free";

	public enum order_state {
		PREPARED,
		DELIVERED,
		PICKEDUP
	}


	private static boolean connect_to_db() throws SQLException, IOException 
	{

		try {
			conn = DBConnector.make_connection();
			return true;
		} catch (SQLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

	}

	public static void addOrder(Order o) throws SQLException, IOException {
		/*
		 * add code to add the order to the DB. Remember that we're not just
		 * adding the order to the order DB table, but we're also recording
		 * the necessary data for the delivery, dinein, pickup, pizzas, toppings
		 * on pizzas, order discounts and pizza discounts.
		 * 
		 * This is a KEY method as it must store all the data in the Order object
		 * in the database and make sure all the tables are correctly linked.
		 * 
		 * Remember, if the order is for Dine In, there is no customer...
		 * so the cusomter id coming from the Order object will be -1.
		 * 
		 */
		connect_to_db();

		String order = "insert into ordertable(ordertable_OrderID, customer_CustID, ordertable_OrderType, ordertable_OrderDateTime, ordertable_CustPrice, ordertable_BusPrice, ordertable_IsComplete) values (?,?,?,?,?,?,?)";
		String dineIn_stmt = "insert into dinein(ordertable_OrderID, dinein_TableNum) values (?,?)";
		String delivery_stmt = "insert into delivery(ordertable_OrderID, delivery_HouseNum, delivery_Street, delivery_City, delivery_State, delivery_Zip, delivery_IsDelivered) values (?,?,?,?,?,?,?)";
		String pickup_stmt = "insert into pickup(ordertable_OrderID, pickup_IsPickedUp) values (?,?)";

		try{
				PreparedStatement prepareStatement = conn.prepareStatement(order);
				prepareStatement.setInt(1,o.getOrderID());
				prepareStatement.setInt(2,o.getCustID());
				prepareStatement.setString(3,o.getOrderType());
				prepareStatement.setString(4,o.getDate());
				prepareStatement.setDouble(5,o.getCustPrice());
				prepareStatement.setDouble(6,o.getBusPrice());
				prepareStatement.setBoolean(7,o.getIsComplete());
				int rowsUpdated = prepareStatement.executeUpdate();

				if (rowsUpdated == 0) {
					System.out.println("Error adding order to ordertable - addOrder");
				}

				if(o instanceof DineinOrder){
					prepareStatement = conn.prepareStatement(dineIn_stmt);
					prepareStatement.setInt(1, o.getOrderID());
					prepareStatement.setInt(2, ((DineinOrder)o).getTableNum());
					rowsUpdated = prepareStatement.executeUpdate();

					if (rowsUpdated == 0) {
						System.out.println("Error adding order to dinein - addOrder");
					}
				}
				if(o instanceof PickupOrder){
					prepareStatement = conn.prepareStatement(pickup_stmt);
					prepareStatement.setInt(1, o.getOrderID());
					prepareStatement.setBoolean(2, ((PickupOrder)o).getIsPickedUp());
					rowsUpdated = prepareStatement.executeUpdate();
						
					if (rowsUpdated == 0) {
						System.out.println("Error adding order to pickup - addOrder");
					}
				}
				else{
					prepareStatement = conn.prepareStatement(delivery_stmt);
					prepareStatement.setInt(1, o.getOrderID());
					prepareStatement.setString(2, ((DeliveryOrder)o).getDate());
					rowsUpdated = prepareStatement.executeUpdate();
						
					if (rowsUpdated == 0) {
						System.out.println("Error adding order to dinein - addOrder");
					}
				}
			}
		catch (SQLException e){
			System.out.println("Error while adding order - addOrder");
		}
	}

	
	public static int addPizza(java.util.Date d, int orderID, Pizza p) throws SQLException, IOException
	{
		/*
		 * Add the code needed to insert the pizza into into the database.
		 * Keep in mind you must also add the pizza discounts and toppings 
		 * associated with the pizza.
		 * 
		 * NOTE: there is a Date object passed into this method so that the Order
		 * and ALL its Pizzas can be assigned the same DTS.
		 * 
		 * This method returns the id of the pizza just added.
		 * 
		 */
		connect_to_db();

		String pizza_stmt = "insert into pizza(pizza_PizzaID, pizza_Size, pizza_CrustType, pizza_PizzaState, pizza_PizzaDate, pizza_CustPrice, pizza_BusPrice, ordertable_OrderID) values (?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement prepareStatement = conn.prepareStatement(pizza_stmt);
			prepareStatement.setInt(1, p.getPizzaID());
			prepareStatement.setString(2, p.getSize());
			prepareStatement.setString(3, p.getCrustType());
			prepareStatement.setString(4, p.getPizzaState());
			prepareStatement.setString(5, p.getPizzaDate());
			prepareStatement.setDouble(6, p.getCustPrice());
			prepareStatement.setDouble(7, p.getBusPrice());
			prepareStatement.setInt(8, orderID);

			int rowsUpdated = prepareStatement.executeUpdate();

			if (rowsUpdated == 0) {
				System.out.println("Error adding pizza to pizza table - addPizza");
			}

		} catch (SQLException e) {
			System.out.println("Error while adding pizza - addPizza");
		}
		return -1;
	}
	
	public static int addCustomer(Customer c) throws SQLException, IOException
	 {
		/*
		 * This method adds a new customer to the database.
		 * 
		 */

		 return -1;
	}

	public static void completeOrder(int OrderID, order_state newState ) throws SQLException, IOException
	{
		/*
		 * Mark that order as complete in the database.
		 * Note: if an order is complete, this means all the pizzas are complete as well.
		 * However, it does not mean that the order has been delivered or picked up!
		 *
		 * For newState = PREPARED: mark the order and all associated pizza's as completed
		 * For newState = DELIVERED: mark the delivery status
		 * FOR newState = PICKEDUP: mark the pickup status
		 * 
		 */

	}


	public static ArrayList<Order> getOrders(int status) throws SQLException, IOException
	 {
	/*
	 * Return an ArrayList of orders.
	 * 	status   == 1 => return a list of open (ie oder is not completed)
	 *           == 2 => return a list of completed orders (ie order is complete)
	 *           == 3 => return a list of all the orders
	 * Remember that in Java, we account for supertypes and subtypes
	 * which means that when we create an arrayList of orders, that really
	 * means we have an arrayList of dineinOrders, deliveryOrders, and pickupOrders.
	 *
	 * You must fully populate the Order object, this includes order discounts,
	 * and pizzas along with the toppings and discounts associated with them.
	 * 
	 * Don't forget to order the data coming from the database appropriately.
	 *
	 */
		return null;
	}
	
	public static Order getLastOrder() throws SQLException, IOException 
	{
		/*
		 * Query the database for the LAST order added
		 * then return an Order object for that order.
		 * NOTE...there will ALWAYS be a "last order"!
		 */
		 return null;
	}

	public static ArrayList<Order> getOrdersByDate(String date) throws SQLException, IOException
	 {
		/*
		 * Query the database for ALL the orders placed on a specific date
		 * and return a list of those orders.
		 *  
		 */
		 return null;
	}
		
	public static ArrayList<Discount> getDiscountList() throws SQLException, IOException 
	{
		/* 
		 * Query the database for all the available discounts and 
		 * return them in an arrayList of discounts ordered by discount name.
		 * 
		*/
		return null;
	}

	public static Discount findDiscountByName(String name) throws SQLException, IOException 
	{
		/*
		 * Query the database for a discount using it's name.
		 * If found, then return an OrderDiscount object for the discount.
		 * If it's not found....then return null
		 *  
		 */
		 return null;
	}


	public static ArrayList<Customer> getCustomerList() throws SQLException, IOException 
	{
		/*
		 * Query the data for all the customers and return an arrayList of all the customers. 
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		*/
		return null;
	}

	public static Customer findCustomerByPhone(String phoneNumber)  throws SQLException, IOException 
	{
		/*
		 * Query the database for a customer using a phone number.
		 * If found, then return a Customer object for the customer.
		 * If it's not found....then return null
		 *  
		 */
		 return null;
	}

	public static String getCustomerName(int CustID) throws SQLException, IOException 
	{
		/*
		 * COMPLETED...WORKING Example!
		 * 
		 * This is a helper method to fetch and format the name of a customer
		 * based on a customer ID. This is an example of how to interact with
		 * your database from Java.  
		 * 
		 * Notice how the connection to the DB made at the start of the 
		 *
		 */

		 connect_to_db();

		/* 
		 * an example query using a constructed string...
		 * remember, this style of query construction could be subject to sql injection attacks!
		 * 
		 */
		String cname1 = "";
		String cname2 = "";
		String query = "Select customer_FName, customer_LName From customer WHERE customer_CustID=" + CustID + ";";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);
		
		while(rset.next())
		{
			cname1 = rset.getString(1) + " " + rset.getString(2); 
		}

		/* 
		* an BETTER example of the same query using a prepared statement...
		* with exception handling
		* 
		*/
		try {
			PreparedStatement os;
			ResultSet rset2;
			String query2;
			query2 = "Select customer_FName, customer_LName From customer WHERE customer_CustID=?;";
			os = conn.prepareStatement(query2);
			os.setInt(1, CustID);
			rset2 = os.executeQuery();
			while(rset2.next())
			{
				cname2 = rset2.getString("customer_FName") + " " + rset2.getString("customer_LName"); // note the use of field names in the getSting methods
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// process the error or re-raise the exception to a higher level
		}

		conn.close();

		return cname1;
		// OR
		// return cname2;

	}


	public static ArrayList<Topping> getToppingList() throws SQLException, IOException 
	{
		/*
		 * Query the database for the aviable toppings and 
		 * return an arrayList of all the available toppings. 
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		 */


		ArrayList<Topping> toppingList = new ArrayList<>();
		connect_to_db(); 

		String query = "SELECT topping_TopID, topping_TopName, topping_SmallAMT, topping_MedAMT, topping_LgAMT, " +
				"topping_XLAMT, topping_CustPrice, topping_BusPrice, topping_MinINVT, topping_CurINVT " +
				"FROM topping " +
				"ORDER BY topping_TopName;";

		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				int topID = rs.getInt("topping_TopID");
				String topName = rs.getString("topping_TopName");
				double smallAMT = rs.getDouble("topping_SmallAMT");
				double medAMT = rs.getDouble("topping_MedAMT");
				double lgAMT = rs.getDouble("topping_LgAMT");
				double xlAMT = rs.getDouble("topping_XLAMT");
				double custPrice = rs.getDouble("topping_CustPrice");
				double busPrice = rs.getDouble("topping_BusPrice");
				int minINVT = rs.getInt("topping_MinINVT");
				int curINVT = rs.getInt("topping_CurINVT");

				// Create a Topping object and add it to the list
				Topping topping = new Topping(topID, topName, smallAMT, medAMT, lgAMT, xlAMT, custPrice, busPrice, minINVT, curINVT);
				toppingList.add(topping);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Error retrieving topping list", e);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close(); 
			}
		}

		return toppingList;
	}

	public static Topping findToppingByName(String name) throws SQLException, IOException 
	{
		/*
		 * Query the database for the topping using it's name.
		 * If found, then return a Topping object for the topping.
		 * If it's not found....then return null
		 *  
		 */
		connect_to_db(); 

		String query = "SELECT topping_TopID, topping_TopName, topping_SmallAMT, topping_MedAMT, topping_LgAMT, " +
				"topping_XLAMT, topping_CustPrice, topping_BusPrice, topping_MinINVT, topping_CurINVT " +
				"FROM topping " +
				"WHERE topping_TopName = ?;";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, name);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int topID = rs.getInt("topping_TopID");
					String name_t = rs.getString("topping_TopName");
					double smallAMT = rs.getDouble("topping_SmallAMT");
					double medAMT = rs.getDouble("topping_MedAMT");
					double lgAMT = rs.getDouble("topping_LgAMT");
					double xlAMT = rs.getDouble("topping_XLAMT");
					double custPrice = rs.getDouble("topping_CustPrice");
					double busPrice = rs.getDouble("topping_BusPrice");
					int minINVT = rs.getInt("topping_MinINVT");
					int curINVT = rs.getInt("topping_CurINVT");

					// Create and return the Topping object
					return new Topping(topID, name_t, smallAMT, medAMT, lgAMT, xlAMT, custPrice, busPrice, minINVT, curINVT);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Error retrieving topping by name", e);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close(); 
			}
		}

		// Return null if no topping is found
		return null;
	}

	public static ArrayList<Topping> getToppingsOnPizza(Pizza p) throws SQLException, IOException 
	{
		/* 
		 * This method builds an ArrayList of the toppings ON a pizza.
		 * The list can then be added to the Pizza object elsewhere in the
		 */

		ArrayList<Topping> toppings = new ArrayList<>();
		connect_to_db(); 

		String query = "SELECT t.topping_TopID, t.topping_TopName, t.topping_SmallAMT, t.topping_MedAMT, " +
				"t.topping_LgAMT, t.topping_XLAMT, t.topping_CustPrice, t.topping_BusPrice, " +
				"t.topping_MinINVT, t.topping_CurINVT, pt.pizza_topping_IsDouble " +
				"FROM pizza_topping pt " +
				"JOIN topping t ON pt.topping_TopID = t.topping_TopID " +
				"WHERE pt.pizza_PizzaID = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, p.getPizzaID()); 
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int topID = rs.getInt("topping_TopID");
					String topName = rs.getString("topping_TopName");
					double smallAMT = rs.getDouble("topping_SmallAMT");
					double medAMT = rs.getDouble("topping_MedAMT");
					double lgAMT = rs.getDouble("topping_LgAMT");
					double xlAMT = rs.getDouble("topping_XLAMT");
					double custPrice = rs.getDouble("topping_CustPrice");
					double busPrice = rs.getDouble("topping_BusPrice");
					int minINVT = rs.getInt("topping_MinINVT");
					int curINVT = rs.getInt("topping_CurINVT");
					boolean isDouble = rs.getBoolean("pizza_topping_isDouble");

					Topping topping = new Topping(topID, topName, smallAMT, medAMT, lgAMT, xlAMT, custPrice, busPrice, minINVT, curINVT);
					topping.setDoubled(isDouble);
					toppings.add(topping);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Error retrieving toppings for the pizza", e);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close(); 
			}
		}

		return toppings;	
	}

	public static void addToInventory(int toppingID, double quantity) throws SQLException, IOException 
	{
		/*
		 * Updates the quantity of the topping in the database by the amount specified.
		 * 
		 * */
		connect_to_db(); 

		String selectQuery = "SELECT ToppingCurrentInventory FROM topping WHERE ToppingID = ?";
		String updateQuery = "UPDATE topping SET ToppingCurrentInventory = ? WHERE ToppingID = ?";

		try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
			PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

			
			selectStmt.setInt(1, toppingID);
			ResultSet rs = selectStmt.executeQuery();

			if (rs.next()) {
				double currentInventory = rs.getDouble("ToppingCurrentInventory");

				
				double newInventory = currentInventory + quantity;
				updateStmt.setDouble(1, newInventory);
				updateStmt.setInt(2, toppingID);

				int rowsAffected = updateStmt.executeUpdate();
				if (rowsAffected == 0) {
					System.out.println("Error updating topping inventory - No rows affected.");
				} else {
					System.out.println("Successfully updated topping inventory.");
				}
			} else {
				System.out.println("Topping ID not found in the database.");
			}
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			while (e.getNextException() != null) {
				e = e.getNextException();
				System.err.println("Next SQL Error: " + e.getMessage());
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
}
	
	
	public static ArrayList<Pizza> getPizzas(Order o) throws SQLException, IOException 
	{
		/*
		 * Build an ArrayList of all the Pizzas associated with the Order.
		 * 
		 */
		
		ArrayList<Pizza> pizzas = new ArrayList<>();
		connect_to_db(); 

		String query = "SELECT p.pizza_PizzaID, p.pizza_CrustType, p.pizza_Size, p.ordertable_OrderID, " +
				"p.pizza_PizzaState, p.pizza_PizzaDate, p.pizza_CustPrice, p.pizza_BusPrice " +
				"FROM pizza p " +
				"WHERE p.ordertable_OrderID = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, o.getOrderID()); // Bind the Order ID to the query
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int pizzaID = rs.getInt("pizza_PizzaID");
					String crustType = rs.getString("pizza_CrustType");
					String size = rs.getString("pizza_Size");
					int orderID = rs.getInt("ordertable_OrderID");
					String state = rs.getString("pizza_PizzaState");
					String date = rs.getString("pizza_PizzaDate");
					double custPrice = rs.getDouble("pizza_CustPrice");
					double busPrice = rs.getDouble("pizza_BusPrice");

					
					Pizza pizza = new Pizza(pizzaID, size, crustType, orderID, state, date, custPrice, busPrice);

					
					ArrayList<Topping> toppings = getToppingsOnPizza(pizza);
					pizza.setToppings(toppings);

					
					ArrayList<Discount> discounts = getDiscounts(pizza);
					pizza.setDiscounts(discounts);

					
					pizzas.add(pizza);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Error retrieving pizzas for the order", e);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close(); // Close the database connection
			}
		}

		return pizzas;
	}

	public static ArrayList<Discount> getDiscounts(Order o) throws SQLException, IOException 
	{
		/* 
		 * Build an array list of all the Discounts associted with the Order.
		 * 
		 */
		ArrayList<Discount> discounts = new ArrayList<>();
		connect_to_db(); 

		String query = "SELECT d.discount_DiscountID, d.discount_DiscountName, d.discount_Amount, d.discount_IsPercent " +
				"FROM order_discount od " +
				"JOIN discount d ON od.discount_DiscountID = d.discount_DiscountID " +
				"WHERE od.ordertable_OrderID = ?";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, o.getOrderID()); 
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int discountID = rs.getInt("discount_DiscountID");
					String discountName = rs.getString("discount_DiscountName");
					double amount = rs.getDouble("discount_Amount");
					boolean isPercent = rs.getBoolean("discount_IsPercent");

					// Create a Discount object
					Discount discount = new Discount(discountID, discountName, amount, isPercent);
					discounts.add(discount);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Error retrieving discounts for the order", e);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close(); 
		}
	}

		return discounts;
	}

	public static ArrayList<Discount> getDiscounts(Pizza p) throws SQLException, IOException 
	{
		/* 
		 * Build an array list of all the Discounts associted with the Pizza.
		 * 
		 */
	
		return null;
	}

	public static double getBaseCustPrice(String size, String crust) throws SQLException, IOException 
	{
		/* 
		 * Query the database fro the base customer price for that size and crust pizza.
		 * 
		*/
		return 0.0;
	}

	public static double getBaseBusPrice(String size, String crust) throws SQLException, IOException 
	{
		/* 
		 * Query the database fro the base business price for that size and crust pizza.
		 * 
		*/
		return 0.0;
	}

	
	public static void printToppingPopReport() throws SQLException, IOException
	{
		/*
		 * Prints the ToppingPopularity view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 * HINT: You need to match the expected output EXACTLY....I would suggest
		 * you look at the printf method (rather that the simple print of println).
		 * It operates the same in Java as it does in C and will make your code
		 * better.
		 * 
		 */
	}
	
	public static void printProfitByPizzaReport() throws SQLException, IOException 
	{
		/*
		 * Prints the ProfitByPizza view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 * HINT: You need to match the expected output EXACTLY....I would suggest
		 * you look at the printf method (rather that the simple print of println).
		 * It operates the same in Java as it does in C and will make your code
		 * better.
		 * 
		 */
	}
	
	public static void printProfitByOrderType() throws SQLException, IOException
	{
		/*
		 * Prints the ProfitByOrderType view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 *
		 * HINT: You need to match the expected output EXACTLY....I would suggest
		 * you look at the printf method (rather that the simple print of println).
		 * It operates the same in Java as it does in C and will make your code
		 * better.
		 * 
		 */
	}
	
	
	
	/*
	 * These private methods help get the individual components of an SQL datetime object. 
	 * You're welcome to keep them or remove them....but they are usefull!
	 */
	private static int getYear(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(0,4));
	}
	private static int getMonth(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(5, 7));
	}
	private static int getDay(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(8, 10));
	}

	public static boolean checkDate(int year, int month, int day, String dateOfOrder)
	{
		if(getYear(dateOfOrder) > year)
			return true;
		else if(getYear(dateOfOrder) < year)
			return false;
		else
		{
			if(getMonth(dateOfOrder) > month)
				return true;
			else if(getMonth(dateOfOrder) < month)
				return false;
			else
			{
				if(getDay(dateOfOrder) >= day)
					return true;
				else
					return false;
			}
	