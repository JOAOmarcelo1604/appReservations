package br.com.dev.jm.web.reservas.service.reservation;

import br.com.dev.jm.web.reservas.entity.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IReservationService {

    public Reservation save(Reservation reservation);
    public Reservation update(Long id, Reservation reservation);
    public Reservation canceled(Long id);
    public Reservation findById(Long id);
    public  void delete(Long id);
    List<Reservation> findAll();
    List<LocalDate> getOccupiedDates(Long unitId);
    List<Reservation> getMinhasReservas(String emailCliente);


}
