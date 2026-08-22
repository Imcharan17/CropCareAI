export default function Recommendations() {
  return <section className="panel"><h3 className="text-xl font-semibold">Treatment Recommendations</h3><div className="mt-4 grid gap-3 md:grid-cols-3">{['Pesticide protocol', 'Fertilizer schedule', 'Prevention plan'].map((item) => <article key={item} className="rounded-md border border-white/10 bg-white/5 p-4"><h4 className="font-semibold">{item}</h4><p className="mt-2 text-sm text-white/60">Attach advice documents and update ticket recommendations from the assigned ticket view.</p></article>)}</div></section>;
}
