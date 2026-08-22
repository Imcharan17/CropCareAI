import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';
import { TicketTable } from '../farmer/TicketTracking.jsx';

export default function AssignedTickets() {
  const [tickets, setTickets] = useState([]);
  useEffect(() => { api.get('/tickets').then(({ data }) => setTickets(data.content || [])).catch(() => {}); }, []);
  return <TicketTable tickets={tickets} />;
}
