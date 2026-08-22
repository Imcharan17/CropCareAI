import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';
import { TicketTable } from '../farmer/TicketTracking.jsx';

export default function TicketManagement() {
  const [tickets, setTickets] = useState([]);

  useEffect(() => {
    api.get('/tickets?size=100').then(({ data }) => setTickets(data.content || [])).catch(() => {});
  }, []);

  return <TicketTable tickets={tickets} />;
}
