import { motion } from 'framer-motion';

export default function StatCard({ label, value }) {
  return (
    <motion.div whileHover={{ y: -3 }} className="panel">
      <p className="text-sm text-white/58">{label}</p>
      <p className="mt-2 text-3xl font-bold text-emerald-300">{value}</p>
    </motion.div>
  );
}
