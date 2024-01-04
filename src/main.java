import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Book;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

import javax.imageio.metadata.IIOMetadataFormat;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import com.mysql.cj.callback.UsernameCallback;

import net.proteanit.sql.DbUtils;

public class main {

	public static class ex {
		public static int days = 0;
	}

	public static void main(String[] args) throws Exception {

		login();

	}

	public static void login() {
		JFrame f = new JFrame("LOGIN");
		JLabel l1, l2;
		l1 = new JLabel("Username");
		l1.setBounds(30, 15, 100, 30);
		l2 = new JLabel("Password");
		l2.setBounds(30, 50, 100, 30);
		JTextField user = new JTextField();
		user.setBounds(110, 15, 200, 30);

		JPasswordField pass = new JPasswordField();
		pass.setBounds(110, 50, 200, 30);

		JButton b1 = new JButton("Login");
		b1.setBounds(130, 90, 80, 25);
		b1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String username = user.getText();
				String password = pass.getText();

				if (username.equals("")) {
					JOptionPane.showMessageDialog(null, "Please enter username");

				} else if (password.equals("")) {
					JOptionPane.showMessageDialog(null, "Please enter password");

				}
				Connection connection = connect();
				try {
					Statement stmt = connection.createStatement();
					String st = "SELECT * FROM users WHERE username=? AND password=?";
					PreparedStatement preparedStatement = connection.prepareStatement(st);
					preparedStatement.setString(1, username);
					preparedStatement.setString(2, password);
					ResultSet rs = preparedStatement.executeQuery();

					if (rs.next() == false) {
						System.out.println("No user");
						JOptionPane.showMessageDialog(null, "Wrong Username/password");
					} else {

						PreparedStatement preparedStatement1 = connection.prepareStatement(st);
						preparedStatement1.setString(1, username);
						preparedStatement1.setString(2, password);
						ResultSet rs1 = preparedStatement1.executeQuery();
						while (rs1.next()) {
							String admin = rs1.getString("ADMIN");
							int UID = rs1.getInt("UID");
							if (admin.equals("1")) {

								f.dispose();
								admin_menu();
							} else {
								f.dispose();
								user_menu(UID);

							}
						}
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}

			}

		});

		f.add(pass);
		f.add(user);
		f.add(l1);
		f.add(l2);
		f.add(b1);
		f.setSize(400, 180);
		f.setLayout(null);
		f.setVisible(true);

	}

	public static Connection connect() {
		try {

			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "Admin123$");
			return con;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void create() throws Exception {
		Connection con = connect();
		ResultSet rs = con.getMetaData().getCatalogs();
		while (rs.next()) {
			String databaseName = rs.getString(1);
			if (databaseName.equals("library")) {
				Statement stmt = con.createStatement();
				String sql = "DROP DATABASE library";
				stmt.executeUpdate(sql);

			}

		}

		Statement stmt = con.createStatement();
		String sql = "CREATE DATABASE library";
		stmt.executeUpdate(sql);
		stmt.executeUpdate("use library");
		String sql1 = "CREATE TABLE USERS(UID int not null auto_increment primary key,username varchar(20),password varchar(20),Admin Boolean)";
		stmt.executeUpdate(sql1);
		stmt.executeUpdate("insert into users(USERNAME,PASSWORD,ADMIN) VALUES('admin','admin',TRUE)");
		String sql2 = "CREATE TABLE BOOKS(BID int not null auto_increment primary key,BNAME varchar(20),GENRE varchar(20),PRICE int)";
		stmt.executeUpdate(sql2);
		String sql3 = "CREATE TABLE ISSUED(IID int not null auto_increment primary key,UID int,BID int,ISSUED_DATE varchar(20), RETURN_DATE varchar(20),PERIOD int,FINE int)";
		stmt.executeUpdate(sql3);
		rs.close();

	}

	public static void user_menu(int uID) throws SQLException {
		JFrame f = new JFrame("User Functions");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JButton view_button = new JButton("View Books");
		view_button.setBounds(20, 20, 120, 25);
		view_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame f1 = new JFrame("Books Available");
				f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				Connection con = connect();
				String sql = "select * from BOOKS";
				try {
					Statement stmt = con.createStatement();
					stmt.executeUpdate("USE LIBRARY");
					stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(sql);
					JTable book_list = new JTable();
					book_list.setModel(DbUtils.resultSetToTableModel(rs));

					JScrollPane scrollpane = new JScrollPane(book_list);
					f1.add(scrollpane);
					f1.setSize(800, 400);
					f1.setVisible(true);
					f1.setLocationRelativeTo(null);

				} catch (SQLException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, e1);

				}

			}
		});

		JButton mybook = new JButton("MY Books");
		mybook.setBounds(150, 20, 120, 25);
		mybook.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JFrame f2 = new JFrame("My Books");
				f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				int UID_int = uID;

				Connection con = connect();
				String sql = "select distinct issued.*,books.bname,books.genre,books.price from issued, books"
						+ " where ((issued.uid=" + UID_int
						+ ")and (books.bid in  (select bid from issued where issued.uid=" + UID_int
						+ "))) group by iid";
				String sql1 = "select bid from issued where uid=" + UID_int;
				Statement stmt;
				try {
					stmt = con.createStatement();
					stmt.executeUpdate("USE library");
					stmt = con.createStatement();
					ArrayList books_list1 = new ArrayList();

					ResultSet rs123 = stmt.executeQuery(sql);
					JTable book_list1 = new JTable();
					book_list1.setModel(DbUtils.resultSetToTableModel(rs123)); 
					JScrollPane scrollpane = new JScrollPane(book_list1);
					f2.add(scrollpane);
					f2.setSize(800, 400);
					f2.setVisible(true);
				} catch (SQLException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, e1);
				}

			}
		});

		f.add(mybook);
		f.add(view_button);
		f.setSize(300, 100);
		f.setLayout(null);
		f.setVisible(true);

	}

	public static void admin_menu() {

		JFrame f = new JFrame("Admin Functions");
		JButton create_button = new JButton("Create/Reset");
		create_button.setBounds(450, 60, 120, 25);
		create_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					create();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				JOptionPane.showMessageDialog(null, "Database Created/Reset");

			}
		});

		JButton add_user = new JButton("Add User");
		add_user.setBounds(20, 60, 120, 25);
		add_user.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				JFrame f4 = new JFrame("Enter User Details");
				JLabel l1, l2;
				l1 = new JLabel("Username");
				l2 = new JLabel("Password");
				l1.setBounds(30, 15, 200, 30);
				l2.setBounds(30, 50, 100, 30);

				JTextField f_user = new JTextField();
				f_user.setBounds(110, 15, 200, 30);
				JTextField f_pass = new JPasswordField();
				f_pass.setBounds(110, 50, 200, 30);
				JRadioButton a1 = new JRadioButton("Admin");
				a1.setBounds(55, 80, 200, 30);
				JRadioButton a2 = new JRadioButton("User");
				a2.setBounds(110, 80, 200, 30);

				ButtonGroup bg = new ButtonGroup();
				bg.add(a1);
				bg.add(a2);

				JButton create_button = new JButton("Create");
				create_button.setBounds(130, 130, 80, 25);
				create_button.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						String username = f_user.getText();
						String password = f_pass.getText();
						Boolean admin = false;
						if (a1.isSelected()) {
							admin = true;
						}

						Connection con = connect();
						try {
							Statement stmt = con.createStatement();

							stmt.executeUpdate("use library");
							stmt.executeUpdate("insert into users(username,password,admin) values('" + username + "','"
									+ password + "'," + admin + ")");
							JOptionPane.showMessageDialog(null, "User add");
							f4.dispose();

						} catch (Exception e2) {
						}
					}
				});
				
				f4.add(l1);
				f4.add(f_user);
				f4.add(l2);
				f4.add(f_pass);
				f4.add(a1);
				f4.add(a2);
				f4.add(create_button);
				f4.setSize(350, 200);
				f4.setLayout(new GridLayout(4,2,1,5));
				f4.setResizable(false);
				f4.setVisible(true);

			}
		});

		JButton add_book = new JButton("Add book");
		add_book.setBounds(150, 60, 120, 25);
		add_book.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame f5 = new JFrame("Enter Book Details");
				JLabel l1, l2, l3;
				JTextField F_bname, F_genre, F_price1;
				l1 = new JLabel("Book Name");
				l1.setBounds(30, 15, 100, 30);

				l2 = new JLabel("Genre");
				l2.setBounds(30, 53, 100, 30);

				l3 = new JLabel("Price");
				l3.setBounds(30, 90, 100, 30);

				F_bname = new JTextField();
				F_bname.setBounds(110, 15, 200, 30);

				F_genre = new JTextField();
				F_genre.setBounds(110, 53, 200, 30);

				F_price1 = new JTextField();
				F_price1.setBounds(110, 90, 200, 30);

				JButton create_button = new JButton("Submit");
				create_button.setBounds(130, 130, 80, 25);

				f5.add(l1);
				f5.add(F_bname);
				f5.add(l2);
				f5.add(F_genre);
				f5.add(l3);
				f5.add(F_price1);
				f5.add(create_button);
				f5.setSize(350, 200);
				f5.setLayout(null);
				f5.setVisible(true);

				create_button.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						String bname = F_bname.getText();
						String genre = F_genre.getText();
						String price = F_price1.getText();

						Connection con = connect();
						try {
							Statement stmt = con.createStatement();
							stmt.executeUpdate("use library");
							stmt.executeUpdate("insert into books(BNAME,GENRE,PRICE) VALUES('" + bname + "','" + genre
									+ "','" + price + "')");
							JOptionPane.showMessageDialog(null, "book added!!!!!!!!");
							f5.dispose();

						} catch (Exception e5) {
							e5.printStackTrace();
							JOptionPane.showMessageDialog(null, e5);
						}

					}
				});

			}
		});

		JButton issue_book = new JButton("Issue Book");
		issue_book.setBounds(450, 20, 120, 25);
		issue_book.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame f6 = new JFrame();
				JLabel l1, l2, l3, l4;
				l1 = new JLabel("Book ID(BID)");
				l1.setBounds(30, 15, 100, 30);

				l2 = new JLabel("User ID(UID)");
				l2.setBounds(30, 53, 100, 30);

				l3 = new JLabel("Period(days)");
				l3.setBounds(30, 90, 100, 30);

				l4 = new JLabel("Issued Date(DD-MM-YYYY)");
				l4.setBounds(30, 127, 150, 30);

				JTextField f_bid = new JTextField();
				f_bid.setBounds(110, 15, 200, 30);

				JTextField f_uid = new JTextField();
				f_uid.setBounds(110, 53, 200, 30);

				JTextField f_period = new JTextField();
				f_period.setBounds(110, 90, 200, 30);

				JTextField f_issue = new JTextField();
				f_issue.setBounds(180, 130, 130, 30);

				JButton create_button1 = new JButton("submit");
				create_button1.setBounds(130, 170, 80, 25);
				create_button1.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						String uid = f_uid.getText();
						String bid = f_bid.getText();
						String period = f_period.getText();
						String issued_date = f_issue.getText();

						int period_int = Integer.parseInt(period);
						Connection con = connect();
						try {
							Statement stmt = con.createStatement();
							stmt.executeUpdate("use library");
							stmt.executeUpdate("insert into issued(UID,BID,ISSUED_DATE,PERIOD)" + "VALUES('" + uid
									+ "','" + bid + "','" + issued_date + "','" + period_int + "')");
							JOptionPane.showMessageDialog(null, "Book Issued");
							f6.dispose();

						} catch (Exception e2) {
						}

					}
				});

				f6.add(l1);
				f6.add(f_bid);
				f6.add(l2);
				f6.add(f_uid);
				f6.add(l3);
				f6.add(f_issue);
				f6.add(l4);
				f6.add(f_period);
				f6.add(create_button1);
				f6.setLayout(null);
				f6.setSize(350, 250);
				f6.setVisible(true);

			}
		});

		JButton return_book = new JButton("Return Book");
		return_book.setBounds(280, 60, 160, 25);
		return_book.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame f7 = new JFrame("Enter Details");
				JLabel l1,l4 ;
				l1 = new JLabel("Issue ID(IID)");
				l1.setBounds(30, 15, 100, 30);
				
				l4=new JLabel("Return Date(DD-MM-YYYY)");
				l4.setBounds(30,50,150,30);
				
				JTextField F_iid = new JTextField();
				F_iid.setBounds(110, 15, 200, 30);

				JTextField F_return = new JTextField();
				F_return.setBounds(180, 50, 130, 30);

				JButton createButton2 = new JButton("Return");
				createButton2.setBounds(130, 170, 80, 25);
				createButton2.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						String iid = F_iid.getText();
						String return_Date = F_return.getText();
						Connection con = connect();
						try {
							Statement stmt = con.createStatement();
							stmt.executeUpdate("use library");
							String date1 = null;
							String date2 = return_Date;

							ResultSet rs = stmt.executeQuery("select issued_date from issued where iid=" + iid);
							while (rs.next()) {
								date1 = rs.getString(1);
							}

							java.util.Date date_1 = new SimpleDateFormat("dd-mm-yyyy").parse(date1);
							java.util.Date date_2 = new SimpleDateFormat("dd-mm-yyyy").parse(date2);

							long diff = date_2.getTime() - date_1.getTime();
							ex.days = (int) (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

							stmt.executeUpdate("update issued set return_date='" + return_Date + "' where iid=" + iid);
							f7.dispose();

							Connection con1 = connect();
							Statement stmt1 = con1.createStatement();
							stmt1.executeUpdate("use library");
							ResultSet rs1 = stmt1.executeQuery("select period from issued where iid=" + iid);
							String diff1 = null;
							while (rs1.next()) {
								diff1 = rs1.getString(1);
							}
							int diff_int = Integer.parseInt(diff1);
						
							if (ex.days > diff_int) {
								int fine = (ex.days - diff_int) * 10;
								stmt1.executeUpdate("UPDATE ISSUED SET FINE=" + fine + " where iid=" + iid);
								String fine_str = ("Fine:" + fine);
								JOptionPane.showMessageDialog(null, fine_str);
							}
							JOptionPane.showMessageDialog(null, "Book Returned");

						} catch (Exception e2) {
						}

					}
				});
				
				f7.add(l4);
				f7.add(createButton2);
				f7.add(l1);
				f7.add(F_iid);
				f7.add(F_return);
				f7.setSize(350, 250);
				f7.setLayout(null);
				f7.setLocationRelativeTo(null);
				f7.setVisible(true);

			}
		});

		JButton view_button1 = new JButton("View Books");
		view_button1.setBounds(20, 20, 120, 25);
		view_button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					JFrame f1 = new JFrame("Books Availiable");
					Connection con = connect();
					String sql = "select * from books";
					Statement stmt = con.createStatement();
					stmt.executeUpdate("use Library");

					stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(sql);
					JTable book_list = new JTable();
					book_list.setModel(DbUtils.resultSetToTableModel(rs));
					JScrollPane scrollpane = new JScrollPane(book_list);
					f1.add(scrollpane);
					f1.setSize(350, 150);
					f1.setVisible(true);
				} catch (Exception e3) {
					e3.printStackTrace();
				}

			}
		});

		JButton users_button = new JButton("View Users");
		users_button.setBounds(150, 20, 120, 25);
		users_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame f2 = new JFrame("Users List");
				Connection con = connect();
				String sql = "select * from users";
				Statement stmt;
				try {
					stmt = con.createStatement();

					stmt.executeUpdate("use library");
					stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(sql);
					JTable book_list = new JTable();
					book_list.setModel(DbUtils.resultSetToTableModel(rs));
					JScrollPane scrollPane = new JScrollPane(book_list);

					f2.add(scrollPane);
					f2.setSize(350, 400);
					f2.setVisible(true);

				} catch (SQLException e1) {
					e1.printStackTrace();
				}

			}
		});

		JButton issued_button = new JButton("View issued Books");
		issued_button.setBounds(280, 20, 160, 25);
		issued_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame f3 = new JFrame("Users List");
				Connection con = connect();
				String sql = "select * from issued";
				try {
					Statement stmt = con.createStatement();
					stmt.executeUpdate("use Library");
					stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(sql);
					JTable book_list = new JTable();
					book_list.setModel(DbUtils.resultSetToTableModel(rs));
					JScrollPane scrollPane = new JScrollPane(book_list);
					f3.add(scrollPane);
					f3.setSize(800, 400);
					f3.setVisible(true);

				} catch (Exception e2) {

					e2.printStackTrace();
				}

			}
		});

		f.add(create_button);
		f.add(return_book);
		f.add(issue_book);
		f.add(add_book);
		f.add(issued_button);
		f.add(users_button);
		f.add(view_button1);
		f.add(add_user);
		f.setSize(600, 200);// 400 width and 500 height
		f.setLayout(null);// using no layout managers
		f.setVisible(true);// making the frame visible
		f.setLocationRelativeTo(null);

	}

}
