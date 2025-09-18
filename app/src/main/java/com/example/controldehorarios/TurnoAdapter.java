package com.example.controldehorarios;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TurnoAdapter extends RecyclerView.Adapter<TurnoAdapter.TurnoViewHolder> {

    private List<Turno> listaTurnos;
    private OnTurnoClickListener listener;

    // Comunicar el click al Activity
    public interface OnTurnoClickListener {
        void onTurnoClick(Turno turno);
    }

    // Constructor
    public TurnoAdapter(List<Turno> listaTurnos, OnTurnoClickListener listener) {
        this.listaTurnos = listaTurnos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TurnoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_turno2, parent, false);
        return new TurnoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull TurnoViewHolder holder, int position) {
        Turno turno = listaTurnos.get(position);

        holder.tvFecha.setText(turno.getFechaInicio() + " al " + turno.getFechaFinal());
        holder.tvHoras.setText(turno.getHoras() + "h " + turno.getMinutos() + "m");
        holder.tvSalario.setText(turno.getTotalPagado() + "€");
        holder.tvNotas.setText((turno.getNotas().isEmpty() ? "(sin notas)" : turno.getNotas()));

        // Detectar clic
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTurnoClick(turno);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTurnos.size();
    }

    public static class TurnoViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvHoras, tvSalario, tvNotas;

        public TurnoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHoras = itemView.findViewById(R.id.tvHoras);
            tvSalario = itemView.findViewById(R.id.tvSalario);
            tvNotas = itemView.findViewById(R.id.tvNotas);
        }
    }
}
