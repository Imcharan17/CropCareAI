import { useEffect, useRef, useState } from 'react';
import { Send } from 'lucide-react';
import SockJS from 'sockjs-client';
import { Stomp } from 'stompjs/lib/stomp.js';
import { useSelector } from 'react-redux';
import { api } from '../../api/client.js';

export default function Chat() {
  const token = useSelector((state) => state.auth.token);
  const [tickets, setTickets] = useState([]);
  const [ticketId, setTicketId] = useState('');
  const [messages, setMessages] = useState([]);
  const [draft, setDraft] = useState('');
  const [online, setOnline] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    api.get('/tickets?size=50').then(({ data }) => {
      const items = data.content || [];
      setTickets(items);
      if (items.length) setTicketId(String(items[0].id));
    });
  }, []);

  useEffect(() => {
    if (!ticketId) return undefined;
    api.get(`/tickets/${ticketId}/messages`).then(({ data }) => setMessages(data));
    const client = Stomp.over(new SockJS('/api/ws'));
    client.debug = () => {};
    client.connect({ Authorization: `Bearer ${token}` }, () => {
      setOnline(true);
      client.subscribe(`/topic/tickets/${ticketId}`, (frame) => {
        const message = JSON.parse(frame.body);
        setMessages((current) => current.some((item) => item.id === message.id) ? current : [...current, message]);
      });
    }, () => setOnline(false));
    clientRef.current = client;
    return () => {
      setOnline(false);
      if (client.connected) client.disconnect();
    };
  }, [ticketId, token]);

  function send(event) {
    event.preventDefault();
    if (!draft.trim() || !clientRef.current?.connected) return;
    clientRef.current.send(`/app/tickets/${ticketId}/chat`, {}, JSON.stringify({ message: draft.trim() }));
    setDraft('');
  }

  return <section className="panel grid min-h-[560px] grid-rows-[auto_1fr_auto]">
    <div className="flex flex-wrap items-center justify-between gap-3 border-b border-white/10 pb-4">
      <select className="input max-w-md" value={ticketId} onChange={(event) => setTicketId(event.target.value)}>
        {tickets.map((ticket) => <option key={ticket.id} value={ticket.id}>#{ticket.id} {ticket.title}</option>)}
      </select>
      <span className={online ? 'text-sm text-emerald-300' : 'text-sm text-white/45'}>{online ? 'Online' : 'Connecting'}</span>
    </div>
    <div className="grid content-start gap-3 overflow-y-auto py-4">
      {messages.map((message) => <div key={message.id} className="max-w-xl rounded-lg bg-white/10 p-3"><p className="text-xs text-emerald-200">{message.senderName}</p><p>{message.message}</p><p className="mt-1 text-xs text-white/40">{new Date(message.createdAt).toLocaleString()}</p></div>)}
      {!messages.length && <p className="text-sm text-white/50">No messages in this ticket yet.</p>}
    </div>
    <form onSubmit={send} className="flex gap-3"><input className="input" value={draft} onChange={(event) => setDraft(event.target.value)} placeholder="Write a message" /><button className="btn" disabled={!online || !ticketId}><Send size={17} /> Send</button></form>
  </section>;
}
