import "./globals.css";

export const metadata = {
  title: "Workchat.pro",
  description: "Best chat",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body className={`${geistSans.variable} ${geistMono.variable}`}>
        {children}
      </body>
    </html>
  );
}
