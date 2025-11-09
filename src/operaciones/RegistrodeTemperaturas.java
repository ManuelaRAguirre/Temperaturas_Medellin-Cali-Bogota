package operaciones;

import java.time.LocalDate;

public class RegistrodeTemperaturas {

    private String ciudad, nombreEstacion;
    private double valor;// (valor se refiere al de la temperatura)
    private LocalDate fecha;

    public RegistrodeTemperaturas(String ciudad, String nombreEstacion, double valor, LocalDate fecha) {

        this.ciudad = ciudad;
        this.nombreEstacion = nombreEstacion;
        this.valor = valor;
        this.fecha = fecha;
    }

    public String getCiudad(){
        return ciudad;
    }

    public String getNombreEstacion(){
        return nombreEstacion;
    }

    public double getValor(){
        return valor;
    }

    public LocalDate getFecha(){
        return fecha;
    }


}
