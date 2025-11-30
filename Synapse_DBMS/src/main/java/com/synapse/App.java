package com.synapse;

import com.synapse.dao.EventsDao;

public class App {
    public static void main(String[] args) throws Exception {
        EventsDao dao = new EventsDao();

        int id = 81;

        boolean gone = dao.delete(id);
        System.out.println("DELETE -> success? " + gone);

        System.out.println("Check after DELETE -> " + dao.findById(id));
    }
}
