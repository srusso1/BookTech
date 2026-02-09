package controllers.Login;

import database.PrestamosDAO;
import database.UsuariosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import model.Usuario;
import utils.Alertas;
import utils.ManagerView;
import utils.Paths;
import utils.Validaciones;

import java.io.IOException;
import java.sql.Connection;
import java.util.Objects;

public class LoginController {

    @FXML
    private Button btnIngresar;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private AnchorPane contenedor;

    @FXML
    private TextField txtUsuario;

    UsuariosDAO usuariosDAO = new UsuariosDAO();
    PrestamosDAO prestamosDAO = new PrestamosDAO();

    @FXML
    void clickIngresar(ActionEvent event) {
        ingresar();
    }

    private void ingresar(){
        if(!Validaciones.campoRequerido(txtUsuario)){
            return;
        }

        if(!Validaciones.validarUsuario(txtUsuario)){
            return;
        }

        if(!Validaciones.campoRequerido(txtPassword)){
            return;
        }

        if(!Validaciones.validarPassword(txtPassword)){
            return;
        }

        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        Usuario user = usuariosDAO.validarUsuario(usuario, password);
        if(user == null){
            Alertas.mostrarError("ERROR: Usuario no encontrado o credenciales incorrectas");
            return;
        }

        Alertas.mostrarExito("Bienvenido " + user.getClass().getSimpleName().toUpperCase() + " " + user.getNombreCompleto());
        if(user.getRol() == 0){
            ManagerView.cargarVista(contenedor, Paths.DASHBOARD_BIBLIOTECARIO);
            prestamosDAO.actualizarPrestamosTarde();
        }else if(user.getRol() == 1){
            ManagerView.cargarVista(contenedor, Paths.DASHBOARD_RECTORIA);
        }


    }

    @FXML
    void initialize(){

    }


}