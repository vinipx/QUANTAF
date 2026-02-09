// @ts-check

import { themes as prismThemes } from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'QUANTAF',
  tagline: 'Quantitative Assurance Next-Gen Test Automation Framework',
  favicon: 'img/logo.svg',

  url: 'https://vinipx.github.io',
  baseUrl: process.env.BASE_URL || '/QUANTAF/',

  organizationName: 'vinipx',
  projectName: 'QUANTAF',

  onBrokenLinks: 'throw',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  markdown: {
    mermaid: true,
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
  },

  themes: ['@docusaurus/theme-mermaid'],

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.js',
          editUrl: 'https://github.com/vinipx/QUANTAF/tree/main/documentation/',
          showLastUpdateTime: true,
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      image: 'img/quantaf-social-card.png',

      mermaid: {
        theme: {
          light: 'base',
          dark: 'dark',
        },
        options: {
          themeVariables: {
            primaryColor: '#1a1a2e',
            primaryTextColor: '#e2e8f0',
            primaryBorderColor: '#22c55e',
            lineColor: '#4ade80',
            secondaryColor: '#1e1e2e',
            tertiaryColor: '#f0fdf4',
          },
        },
      },

      announcementBar: {
        id: 'quantaf_v1',
        content:
          '⚡ QUANTAF — Enterprise-Grade Financial Systems Test Automation. <a target="_blank" rel="noopener noreferrer" href="https://github.com/vinipx/QUANTAF">Star us on GitHub</a>',
        backgroundColor: '#111111',
        textColor: '#d4d4d8',
        isCloseable: true,
      },

      navbar: {
        title: 'QUANTAF',
        logo: {
          alt: 'QUANTAF Logo',
          src: 'img/logo.svg',
          srcDark: 'img/logo-dark.svg',
          width: 36,
          height: 36,
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'docsSidebar',
            position: 'left',
            label: 'Documentation',
          },
          {
            to: '/docs/architecture',
            label: 'Architecture',
            position: 'left',
          },
          {
            to: '/docs/examples',
            label: 'Examples',
            position: 'left',
          },
          {
            href: 'https://github.com/vinipx/QUANTAF',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },

      footer: {
        style: 'dark',
        links: [
          {
            title: 'Documentation',
            items: [
              { label: 'Overview', to: '/docs/overview' },
              { label: 'Architecture', to: '/docs/architecture' },
              { label: 'Configuration', to: '/docs/configuration' },
            ],
          },
          {
            title: 'Framework',
            items: [
              { label: 'Features', to: '/docs/features' },
              { label: 'Examples', to: '/docs/examples' },
              { label: 'Tech Stack', to: '/docs/tech-stack' },
            ],
          },
          {
            title: 'Engineering',
            items: [
              { label: 'Development', to: '/docs/development' },
              { label: 'CI/CD & Docker', to: '/docs/cicd' },
              { label: 'Contributing', to: '/docs/contributing' },
            ],
          },
          {
            title: 'Links',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/vinipx/QUANTAF',
              },
              {
                label: 'Allure Reports',
                to: '/docs/allure',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} QUANTAF — MIT License`,
      },

      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
        additionalLanguages: [
          'java',
          'kotlin',
          'groovy',
          'yaml',
          'bash',
          'json',
          'markup',
          'ini',
          'properties',
          'gherkin',
        ],
      },

      colorMode: {
        defaultMode: 'dark',
        disableSwitch: false,
        respectPrefersColorScheme: true,
      },

      tableOfContents: {
        minHeadingLevel: 2,
        maxHeadingLevel: 4,
      },
    }),
};

export default config;
