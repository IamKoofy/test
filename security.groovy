import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import { resolve } from "path";

export default defineConfig({
  define: {
    'globalThis.crypto': 'require("crypto").webcrypto'
  },
  plugins: [
    vue({
      template: {
        compilerOptions: {
          isCustomElement: (tag) =>
            ["tableau-viz", "tableau-authoring-viz", "tableau-ask-data", "viz-filter", "VizFilter"].includes(tag),
        },
      },
    }),
  ],
  resolve: {
    alias: {
      "@": resolve(__dirname, "src"),
    },
  },
});
