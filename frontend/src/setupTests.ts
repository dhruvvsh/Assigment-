import '@testing-library/jest-dom'
import { TextDecoder, TextEncoder } from 'util'

// react-router expects these browser globals, which Jest's jsdom environment does not provide.
Object.assign(globalThis, { TextEncoder, TextDecoder })
