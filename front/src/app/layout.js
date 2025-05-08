import "./globals.css";
import "@/lib/fontawesome"

export const metadata = {
  title: "Workchat.pro",
  description: "Best chat",
};

export default function RootLayout({ children }) {
  return (
    <html lang="ru">
      <body>
        {children}
      </body>
    </html>
  );
}
