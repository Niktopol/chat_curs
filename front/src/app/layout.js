import "./globals.css";
import "@/lib/fontawesome"
import { Providers } from "./providers";

export const metadata = {
  title: "Workchat.pro",
  description: "Best chat",
};

export default function RootLayout({ children }) {
  return (
    <html lang="ru">
      <body>
        <Providers>
          {children}
        </Providers>
      </body>
    </html>
  );
}
