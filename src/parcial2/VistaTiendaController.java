/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package parcial2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Producto;

/**
 * FXML Controller class
 *
 * @author Fede
 */
public class VistaTiendaController implements Initializable {

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    @FXML private TextField txtCantidad;
    @FXML private ListView<String> listaCarrito; 
    @FXML private Label lblMensaje;

    private ObservableList<Producto> listaProductos; 
    private final String NOMBRE_ARCHIVO = "productos.dat";
    
    private ArrayList<Producto> productosComprados;
    private ArrayList<Integer> cantidadesCompradas;
    
    private void cargarProductos() {
        File archivo = new File(NOMBRE_ARCHIVO);
        if (archivo.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                // Lee la lista
                ArrayList<Producto> lista = (ArrayList<Producto>) ois.readObject();
                listaProductos.setAll(lista);
            } catch (Exception e) {
                lblMensaje.setText("Error al cargar productos del archivo.");
            }
        } else {
            lblMensaje.setText("No se encontró " + NOMBRE_ARCHIVO);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        listaProductos = FXCollections.observableArrayList();
        tablaProductos.setItems(listaProductos);
        
        productosComprados = new ArrayList<>();
        cantidadesCompradas = new ArrayList<>();

        cargarProductos();
    }  
    
    private void guardarCambiosEnDat() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOMBRE_ARCHIVO))) {
            oos.writeObject(new ArrayList<>(listaProductos)); // Guardar la lista actualizada
        } catch (IOException e) {
            lblMensaje.setText("Error crítico: No se pudo actualizar el stock.");
        }
    }
    
    @FXML
    private void agregar(ActionEvent event) {
        //mensajes de error
        lblMensaje.setText("");

        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            lblMensaje.setText("Seleccione un producto de la tabla.");
            return;
        }

        int cantidad = 0;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText());
        } catch (NumberFormatException e) {
            lblMensaje.setText("La cantidad debe ser un número entero.");
            return;
        }

        if (cantidad <= 0) {
            lblMensaje.setText("La cantidad debe ser mayor a 0.");
            return;
        }

        if (cantidad > seleccionado.getStock()) {
            lblMensaje.setText("Stock insuficiente");
            return;
        }
        
        // Actualizar stock
        
        seleccionado.setStock(seleccionado.getStock() - cantidad);
        tablaProductos.refresh(); 

        // Agregar al carrito
        String articulo = seleccionado.getNombre() + " Cant: " + cantidad;
        listaCarrito.getItems().add(articulo);

        // Agregar a las listas para actualizar
        productosComprados.add(seleccionado);
        cantidadesCompradas.add(cantidad);

        // Limpiar campo de cantidad
        txtCantidad.setText("");
    }
    
    @FXML
    private void confirmar(ActionEvent event) {
        //Mensajes de error
        lblMensaje.setText("");

        if (productosComprados.isEmpty()) {
            lblMensaje.setText("El carrito está vacío.");
            return;
        }

        // Escribit el ticket
        try {
            FileWriter fw = new FileWriter("ticket.txt");
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("...TICKET DE COMPRA...");
            bw.newLine();
            bw.write("Producto          Cant      Subtotal");
            bw.newLine();
            bw.write("------------------------------------");
            bw.newLine();
            
            // contador para precio total
            double totalGeneral = 0;

            // recorro la lista de productos comprados
            for (int i = 0; i < productosComprados.size(); i++) {
                
                Producto p = productosComprados.get(i);
                int cant = cantidadesCompradas.get(i); // para recorrer la lista de las cantidades
                
                //Calcula el precio por producto y lo suma al total general
                double subtotal = p.getPrecio() * cant;
                totalGeneral = totalGeneral + subtotal;

                //escribir en el ticket
                bw.write(p.getNombre() + "    " + cant + "      $" + subtotal);
                bw.newLine();
            }
            
            bw.write("...");
            bw.newLine();
            bw.write("TOTAL A PAGAR: $" + totalGeneral);

            bw.close(); // Cerrar ticket

            // guardar el nuevo stock en el archivo .dat de productos
            guardarCambiosEnDat();

            // limpiar campos
            listaCarrito.getItems().clear();
            productosComprados.clear();
            cantidadesCompradas.clear();
            
            lblMensaje.setText("Compra finalizada.");

        } catch (IOException e) {
            lblMensaje.setText("Error al generar el ticket.");
        }
    }
    
}
