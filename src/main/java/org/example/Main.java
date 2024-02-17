package org.example;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String URL = "jdbc:mysql://localhost:3306/reserves";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\nMenú:");
                System.out.println("1. Insertar les reserves a la base de dades");
                System.out.println("2. Suprimir totes les dades de les reserves");
                System.out.println("3. Consultar les dades d’una reserva");
                System.out.println("4. Consulta de reserves per agència");
                System.out.println("5. Insertar una reserva");
                System.out.println("6. Eliminar una reserva");
                System.out.println("7. Modificar una reserva");
                System.out.println("8. Sortir");

                System.out.print("Selecciona una opció: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1:
                        insertReservesFromXML(connection, "bookings.xml");
                        break;
                    case 2:
                        deleteReserves(connection);
                        break;
                    case 3:
                        showReserveDetails(connection);
                        break;
                    case 4:
                        showReservesByAgency(connection);
                        break;
                    case 5:
                        insertReserve(connection);
                        break;
                    case 6:
                        deleteReserve(connection);
                        break;
                    case 7:
                        updateReserve(connection);
                        break;
                    case 8:
                        System.out.println("Sortint...");
                        return;
                    default:
                        System.out.println("Opció no vàlida. Torna a provar.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertReservesFromXML(Connection connection, String xmlFileName) {

        try {
            File xmlFile = new File("bookings.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            NodeList bookingsNodeList = document.getElementsByTagName("booking");

            for (int i = 0; i < bookingsNodeList.getLength(); i++) {
                Node bookingNode = bookingsNodeList.item(i);
                if (bookingNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element bookingElement = (Element) bookingNode;

                    String locationNumber = bookingElement.getAttribute("location_number");
                    String client = bookingElement.getElementsByTagName("client").item(0).getTextContent();
                    String agency = bookingElement.getElementsByTagName("agency").item(0).getTextContent();
                    String price = bookingElement.getElementsByTagName("price").item(0).getTextContent();
                    String room = bookingElement.getElementsByTagName("room").item(0).getTextContent();
                    String hotel = bookingElement.getElementsByTagName("hotel").item(0).getTextContent();
                    String checkInStr = bookingElement.getElementsByTagName("check_in").item(0).getTextContent();
                    int roomNights = Integer.parseInt(bookingElement.getElementsByTagName("room_nights").item(0).getTextContent());

                    try (PreparedStatement preparedStatement = connection.prepareStatement(
                            "INSERT INTO reserves (Locator, Client, Agency, Price, Room, Hotel, Check_in, Room_nights) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                        preparedStatement.setString(1, locationNumber);
                        preparedStatement.setString(2, client);
                        preparedStatement.setString(3, agency);
                        preparedStatement.setString(4, price);
                        preparedStatement.setString(5, room);
                        preparedStatement.setString(6, hotel);
                        preparedStatement.setDate(7, Date.valueOf(parseDate(checkInStr)));
                        preparedStatement.setInt(8, roomNights);

                        preparedStatement.executeUpdate();
                    }
                }
            }
            System.out.println("Reservas insertadas desde el archivo XML.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String parseDate(String dateStr) {
        String[] parts = dateStr.split("/");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }


    private static void deleteReserves(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            String sql = "DELETE FROM reserves";
            int rowsAffected = statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }}


    private static void showReserveDetails(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introdueix el localitzador de la reserva: ");
        String locator = scanner.nextLine();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM reserves WHERE Locator = ?")) {
            preparedStatement.setString(1, locator);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Detalls de la reserva:");
                System.out.println("Client: " + resultSet.getString("Client"));
                System.out.println("Agency: " + resultSet.getString("Agency"));
                System.out.println("Price: " + resultSet.getString("Price"));
                System.out.println("Room: " + resultSet.getString("Room"));
                System.out.println("Hotel: " + resultSet.getString("Hotel"));
                System.out.println("Check_in: " + resultSet.getDate("Check_in"));
                System.out.println("Room_nights: " + resultSet.getString("Room_nights"));
            } else {
                System.out.println("No s'ha trobat cap reserva amb aquest localitzador.");
            }
        }
    }
    private static void showReservesByAgency(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introdueix l'identificador de l'agència: ");
        String agencyId = scanner.nextLine();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM reserves WHERE Agency = ?")) {
            preparedStatement.setString(1, agencyId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                System.out.println("Reserva:");
                System.out.println("Client: " + resultSet.getString("Client"));
                System.out.println("Price: " + resultSet.getString("Price"));
                System.out.println("Room: " + resultSet.getString("Room"));
                System.out.println();
            }
        }
    }

    private static void insertReserve(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Locator: ");
        String Locator = scanner.nextLine();
        System.out.print("Client: ");
        String client = scanner.nextLine();
        System.out.print("Agency: ");
        String agency = scanner.nextLine();
        System.out.print("Price: ");
        String price = scanner.nextLine();
        System.out.print("Room: ");
        String room = scanner.nextLine();
        System.out.print("Hotel: ");
        String hotel = scanner.nextLine();
        System.out.print("Check_in (YYYY-MM-DD): ");
        String checkInDate = scanner.nextLine();
        System.out.print("Room_night: ");
        int roomNight = scanner.nextInt();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO reserves (Locator,Client, Agency, Price, Room, Hotel, Check_in, Room_nights) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            preparedStatement.setString(1, Locator);
            preparedStatement.setString(2, client);
            preparedStatement.setString(3, agency);
            preparedStatement.setString(4, price);
            preparedStatement.setString(5, room);
            preparedStatement.setString(6, hotel);
            preparedStatement.setDate(7, Date.valueOf(checkInDate));
            preparedStatement.setInt(8, roomNight);

            preparedStatement.executeUpdate();
        }

        System.out.println("Reserva afegida correctament.");
    }

    private static void deleteReserve(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introdueix el localitzador de la reserva a eliminar: ");
        String locator = scanner.nextLine();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM reserves WHERE Locator = ?")) {
            preparedStatement.setString(1, locator);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Reserva eliminada correctament.");
            } else {
                System.out.println("No s'ha trobat cap reserva amb aquest localitzador.");
            }
        }
    }

    private static void updateReserve(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introdueix el localitzador de la reserva a modificar: ");
        String locator = scanner.nextLine();

        if (!reserveExists(connection, locator)) {
            System.out.println("No s'ha trobat cap reserva amb aquest localitzador.");
            return;
        }
        System.out.print("Nou client: ");
        String newClient = scanner.nextLine();
        System.out.print("Nova agència: ");
        String newAgency = scanner.nextLine();
        System.out.print("Nou preu: ");
        double newPrice = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Nova habitació: ");
        String newRoom = scanner.nextLine();
        System.out.print("Nou hotel: ");
        String newHotel = scanner.nextLine();
        System.out.print("Nova data de check-in (YYYY-MM-DD): ");
        String newCheckInDate = scanner.nextLine();
        System.out.print("Nova nit d'habitació: ");
        int newRoomNight = scanner.nextInt();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE reserves SET Client = ?, Agency = ?, Price = ?, Room = ?, Hotel = ?, Check_in = ?, Room_nights = ? " +
                        "WHERE Locator = ?")) {
            preparedStatement.setString(1, newClient);
            preparedStatement.setString(2, newAgency);
            preparedStatement.setDouble(3, newPrice);
            preparedStatement.setString(4, newRoom);
            preparedStatement.setString(5, newHotel);
            preparedStatement.setDate(6, Date.valueOf(newCheckInDate));
            preparedStatement.setInt(7, newRoomNight);
            preparedStatement.setString(8, locator);

            preparedStatement.executeUpdate();
        }

        System.out.println("Reserva actualitzada correctament.");
    }

    private static boolean reserveExists(Connection connection, String locator) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM reserves WHERE Locator = ?")) {
            preparedStatement.setString(1, locator);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();   }
    }
}