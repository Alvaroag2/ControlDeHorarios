package com.example.controldehorarios;

public class Turno {
    private int id;
    private String fechaInicio;
    private String fechaFinal;
    private int horas;
    private int minutos;
    private String notas;
    private double totalPagado;

    public Turno(int id, String fechaInicio, String fechaFinal, int horas, int minutos, String notas, double totalPagado) {
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
        this.horas = horas;
        this.minutos = minutos;
        this.notas = notas;
        this.totalPagado = totalPagado;
    }

    public int getId() { return id; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFinal() { return fechaFinal; }
    public int getHoras() { return horas; }
    public int getMinutos() { return minutos; }
    public String getNotas() { return notas; }
    public double getTotalPagado() { return totalPagado; }
}
