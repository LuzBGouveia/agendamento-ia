package br.com.fiap.agendamentocomia.tools;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import br.com.fiap.agendamentocomia.model.Appointment;
import br.com.fiap.agendamentocomia.model.AppointmentStatus;
import br.com.fiap.agendamentocomia.repository.AppointmentRepository;

@Component
public class AppointmentTool {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentRepository repository;

    public AppointmentTool(AppointmentRepository repository) {
        this.repository = repository;
    }

    @Tool(description = "Retorna a data de hoje no formato dd/MM/yyyy. Use sempre que o cliente falar 'hoje', 'amanhã', 'semana que vem', etc.")
    public String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    @Tool(description = "Agenda um novo serviço para um cliente")
    public String schedule(
            @ToolParam(description = "Nome do cliente") String name,
            @ToolParam(description = "Serviço desejado, ex: Corte de Cabelo, Manicure, Massagem") String service,
            @ToolParam(description = "Data no formato dd/MM/yyyy") String date,
            @ToolParam(description = "Hora no formato HH:mm") String time) {

        try {
            LocalDate.parse(date, DATE_FORMAT);
            LocalTime.parse(time, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return "Data ou hora inválida. Use dd/MM/yyyy e HH:mm.";
        }

        var appointment = repository.save(
                new Appointment(null, name, service, date, time, AppointmentStatus.SCHEDULED));

        return "Agendamento #" + appointment.getId() + " criado para " + name
                + " (" + service + ") em " + date + " às " + time + ".";
    }

    @Tool(description = "Cancela um agendamento existente pelo id")
    public String cancel(@ToolParam(description = "Id do agendamento") Long id) {
        return repository.findById(id).map(appointment -> {
            appointment.setStatus(AppointmentStatus.CANCELED);
            repository.save(appointment);
            return "Agendamento #" + id + " cancelado com sucesso.";
        }).orElse("Não encontrei nenhum agendamento com o id " + id + ".");
    }

    @Tool(description = "Reagenda um agendamento existente para uma nova data e hora")
    public String reschedule(
            @ToolParam(description = "Id do agendamento") Long id,
            @ToolParam(description = "Nova data no formato dd/MM/yyyy") String date,
            @ToolParam(description = "Nova hora no formato HH:mm") String time) {

        try {
            LocalDate.parse(date, DATE_FORMAT);
            LocalTime.parse(time, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return "Data ou hora inválida. Use dd/MM/yyyy e HH:mm.";
        }

        return repository.findById(id).map(appointment -> {
            appointment.setAppointmentDate(date);
            appointment.setAppointmentTime(time);
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            repository.save(appointment);
            return "Agendamento #" + id + " reagendado para " + date + " às " + time + ".";
        }).orElse("Não encontrei nenhum agendamento com o id " + id + ".");
    }
}