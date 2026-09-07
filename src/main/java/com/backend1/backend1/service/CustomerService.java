package com.backend1.backend1.service;

import com.backend1.backend1.dto.CustomerDTO;
import com.backend1.backend1.form.CustomerForm;
import com.backend1.backend1.model.Customer;
import com.backend1.backend1.repository.BookingRepository;
import com.backend1.backend1.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;

    //intessant att prata om J
    @Transactional(readOnly = true)
    public List<CustomerDTO> findAll() {
        Map<Long, Long> bookingCounts = bookingRepository.findAll().stream()
                .filter(b -> b.getCustomer() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getCustomer().getId(),
                        Collectors.counting()));
        return customerRepository.findAll().stream()
                .map(c -> toDTO(c, bookingCounts.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerDTO findById(Long id) {
        return customerRepository.findById(id)
                .map(c -> toDTO(c, bookingRepository.countByCustomerId(c.getId())))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Kund med id " + id + " hittades inte"));
    }

    @Transactional
    public void save(CustomerForm form) {
        customerRepository.save(toEntity(form));
    }

    @Transactional
    public void delete(Long id) {
        if (bookingRepository.existsByCustomerId(id)) {
            throw new IllegalStateException(
                    "Kan inte ta bort kund som har aktiva bokningar");
        }
        customerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return customerRepository.count();
    }

    private CustomerDTO toDTO(Customer c, long bookingCount) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(c.getId());
        dto.setFirstName(c.getFirstName());
        dto.setLastName(c.getLastName());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setAddress(c.getAddress());
        dto.setBookingCount(bookingCount);
        return dto;
    }

    private Customer toEntity(CustomerForm form) {
        Customer c = new Customer();
        c.setId(form.getId());
        c.setFirstName(form.getFirstName());
        c.setLastName(form.getLastName());
        c.setEmail(form.getEmail());
        c.setPhone(form.getPhone());
        c.setAddress(form.getAddress());
        return c;
    }
}
