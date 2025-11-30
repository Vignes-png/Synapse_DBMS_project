package com.synapse.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.synapse.dao.EventsDao;
import com.synapse.model.Event;

public class EventsUI extends JFrame {
    private final EventsDao dao = new EventsDao();

    // Inputs
    private final JTextField tfId          = new JTextField(6);
    private final JTextField tfName        = new JTextField(16);
    private final JTextField tfDesc        = new JTextField(16);
    private final JTextField tfType        = new JTextField(12);
    private final JTextField tfSchedule    = new JTextField(16); // 2025-11-09T14:30
    private final JTextField tfPrize       = new JTextField(8);
    private final JTextField tfVenueId     = new JTextField(6);

    // Table
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID","Name","Desc","Type","Schedule","Prize","Venue"}, 0);
    private final JTable table = new JTable(model);

    public EventsUI() {
        super("Events - CRUD (Swing)");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        addRow(form, c, r++, "ID (for Find/Update/Delete):", tfId);
        addRow(form, c, r++, "Name:", tfName);
        addRow(form, c, r++, "Description:", tfDesc);
        addRow(form, c, r++, "Type:", tfType);
        addRow(form, c, r++, "Schedule (YYYY-MM-DDTHH:MM):", tfSchedule);
        addRow(form, c, r++, "Prize:", tfPrize);
        addRow(form, c, r++, "Venue ID:", tfVenueId);

        JButton btnAdd    = new JButton("Add");
        JButton btnList   = new JButton("List All");
        JButton btnFind   = new JButton("Find by ID");     // NEW
        JButton btnUpdate = new JButton("Update by ID");
        JButton btnDelete = new JButton("Delete by ID");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(btnAdd);
        buttons.add(btnList);
        buttons.add(btnFind);   // NEW
        buttons.add(btnUpdate);
        buttons.add(btnDelete);

        JScrollPane scroll = new JScrollPane(table);

        setLayout(new BorderLayout(8,8));
        add(form, BorderLayout.NORTH);
        add(buttons, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);

        // Button actions
        btnAdd.addActionListener(e -> onAdd());
        btnList.addActionListener(e -> refreshTable());
        btnFind.addActionListener(e -> onFindById());      // NEW
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());

        // Press Enter in ID field to trigger find (nice UX)
        tfId.addActionListener(e -> onFindById());         // NEW

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 540);
        setLocationRelativeTo(null);
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; p.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1; p.add(field, c);
    }

    private void onAdd() {
        try {
            String name  = tfName.getText().trim();
            String desc  = tfDesc.getText().trim();
            String type  = tfType.getText().trim();
            LocalDateTime when = parseDate(tfSchedule.getText().trim());
            BigDecimal prize   = new BigDecimal(tfPrize.getText().trim());
            int venueId        = Integer.parseInt(tfVenueId.getText().trim());

            if (name.isEmpty() || type.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Type are required.");
                return;
            }

            Integer id = dao.create(new Event(name, desc, type, when, prize, venueId));
            JOptionPane.showMessageDialog(this, "Inserted! New ID = " + id);
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Prize and Venue ID must be numbers.");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Schedule must be like 2025-11-09T14:30");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Add failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void onFindById() { // NEW
        try {
            String idText = tfId.getText().trim();
            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter an ID first.");
                return;
            }
            int id = Integer.parseInt(idText);

            Event e = dao.findById(id);
            model.setRowCount(0);
            if (e == null) {
                JOptionPane.showMessageDialog(this, "No record found for ID = " + id);
                return;
            }

            // Show in table
            model.addRow(new Object[]{
                e.getEventId(), e.getEventName(), e.getEventDescription(),
                e.getEventType(), e.getSchedule(), e.getPrizeMoney(), e.getVenueId()
            });

            // Populate form (optional but handy)
            tfName.setText(e.getEventName());
            tfDesc.setText(e.getEventDescription());
            tfType.setText(e.getEventType());
            String sched = e.getSchedule().toString(); // 2025-11-09T14:30:00
            tfSchedule.setText(sched.length() >= 16 ? sched.substring(0,16) : sched);
            tfPrize.setText(e.getPrizeMoney().toPlainString());
            tfVenueId.setText(String.valueOf(e.getVenueId()));

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID must be a number.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Find failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void onUpdate() {
        try {
            int id          = Integer.parseInt(tfId.getText().trim());
            String name     = tfName.getText().trim();
            String desc     = tfDesc.getText().trim();
            String type     = tfType.getText().trim();
            LocalDateTime when = parseDate(tfSchedule.getText().trim());
            BigDecimal prize   = new BigDecimal(tfPrize.getText().trim());
            int venueId        = Integer.parseInt(tfVenueId.getText().trim());

            boolean ok = dao.update(id, name, desc, type, when, prize, venueId);
            JOptionPane.showMessageDialog(this, ok ? "Updated!" : "No row updated.");
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID/Venue/Prize must be numbers.");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Schedule must be like 2025-11-09T14:30");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void onDelete() {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            boolean ok = dao.delete(id);
            JOptionPane.showMessageDialog(this, ok ? "Deleted!" : "No row deleted.");
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID must be a number.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void refreshTable() {
        try {
            List<Event> rows = dao.findAll();
            model.setRowCount(0);
            for (Event e : rows) {
                model.addRow(new Object[]{
                        e.getEventId(),
                        e.getEventName(),
                        e.getEventDescription(),
                        e.getEventType(),
                        e.getSchedule(),
                        e.getPrizeMoney(),
                        e.getVenueId()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "List failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private LocalDateTime parseDate(String s) {
        // Expect: 2025-11-09T14:30
        return LocalDateTime.parse(s);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventsUI().setVisible(true));
    }
}